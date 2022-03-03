/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.android.data

import android.app.Notification
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.IBinder
import android.os.PowerManager
import androidx.core.content.ContextCompat
import ca.gosyer.core.lang.throwIfCancellation
import ca.gosyer.core.logging.CKLogger
import ca.gosyer.core.prefs.getAsFlow
import ca.gosyer.data.base.WebsocketService
import ca.gosyer.data.download.DownloadService
import ca.gosyer.data.download.model.DownloadState
import ca.gosyer.data.download.model.DownloadStatus
import ca.gosyer.data.server.requests.downloadsQuery
import ca.gosyer.i18n.MR
import ca.gosyer.jui.android.R
import ca.gosyer.jui.android.util.acquireWakeLock
import ca.gosyer.jui.android.util.notification
import ca.gosyer.jui.android.util.notificationBuilder
import ca.gosyer.jui.android.util.notificationManager
import ca.gosyer.ui.AppComponent
import dev.icerock.moko.resources.desc.desc
import dev.icerock.moko.resources.format
import io.ktor.client.features.websocket.ws
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.readText
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.job
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class AndroidDownloadService : Service() {

    enum class Actions {
        STOP,
        START,
        RESTART
    }

    companion object : CKLogger({}) {
        val running = MutableStateFlow(true)

        private var instance: AndroidDownloadService? = null


        fun start(context: Context, actions: Actions) {
            if (!isRunning() && actions != Actions.STOP) {
                val intent = Intent(context, AndroidDownloadService::class.java).apply {
                    action = actions.name
                }
                ContextCompat.startForegroundService(context, intent)
            }
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, AndroidDownloadService::class.java))
        }

        fun isRunning(): Boolean {
            return instance != null
        }
    }

    /**
     * Wake lock to prevent the device to enter sleep mode.
     */
    private lateinit var wakeLock: PowerManager.WakeLock

    private lateinit var ioScope: CoroutineScope

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        ioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        startForeground(Notifications.ID_DOWNLOAD_CHAPTER_RUNNING, getPlaceholderNotification())
        wakeLock = acquireWakeLock(javaClass.name)
        running.value = true
    }

    override fun onDestroy() {
        ioScope.cancel()
        running.value = false
        wakeLock.releaseIfNeeded()
        notificationManager.cancel(Notifications.ID_DOWNLOAD_CHAPTER_PROGRESS)
        if (instance == this) {
            instance = null
        }
        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        instance = this
        ioScope.coroutineContext.job.cancelChildren()

        if (intent != null) {
            val action = intent.action
            info("using an intent with action $action")
            when (action) {
                Actions.START.name -> startWebsocket()
                Actions.STOP.name -> stopSelf()
                else -> info("This should never happen. No action in the received intent")
            }
        } else {
            info(
                "with a null intent. It has been probably restarted by the system."
            )
            startWebsocket()
        }

        return START_STICKY
    }

    private fun startWebsocket() {
        ioScope.coroutineContext.job.cancelChildren()
        ioScope.coroutineContext.job.invokeOnCompletion {
            stopSelf()
        }

        val dataComponent = AppComponent.getInstance(applicationContext)
            .dataComponent
        val client = dataComponent.http

        var errorConnectionCount = 0

        dataComponent
            .serverPreferences
            .serverUrl()
            .getAsFlow()
            .mapLatest { serverUrl ->
                DownloadService.status.value = WebsocketService.Status.STARTING
                while (true) {
                    if (errorConnectionCount > 3) {
                        DownloadService.status.value = WebsocketService.Status.STOPPED
                        throw CancellationException()
                    }
                    runCatching {
                        client.ws(
                            host = serverUrl.substringAfter("://"),
                            path = downloadsQuery()
                        ) {
                            errorConnectionCount = 0
                            DownloadService.status.value = WebsocketService.Status.RUNNING
                            send(Frame.Text("STATUS"))

                            incoming.receiveAsFlow()
                                .filterIsInstance<Frame.Text>()
                                .mapLatest(::onReceived)
                                .catch {
                                    info(it) { "Error running downloader" }
                                }
                                .collect()
                        }
                    }.throwIfCancellation().isFailure.let {
                        DownloadService.status.value = WebsocketService.Status.STARTING
                        if (it) errorConnectionCount++
                    }
                }
            }
            .catch {
                DownloadService.status.value = WebsocketService.Status.STOPPED
                error(it) { "Error while running websocket service" }
                stopSelf()
            }
            .launchIn(ioScope)
    }

    private fun onReceived(frame: Frame.Text) {
        val status = Json.decodeFromString<DownloadStatus>(frame.readText())
        DownloadService.downloaderStatus.value = status.status
        DownloadService.downloadQueue.value = status.queue
        val downloadingChapter = status.queue.lastOrNull { it.state == DownloadState.Downloading }
        if (downloadingChapter != null) {
            val notification = with(progressNotificationBuilder) {
                val max = downloadingChapter.chapter.pageCount ?: 0
                val current = downloadingChapter.progress.toInt().coerceAtMost(max)
                setProgress(max, current, false)
                setContentText(
                    MR.strings.chapter_downloading_progress
                        .format(
                            current,
                            max
                        )
                        .toString(this@AndroidDownloadService)
                )
            }.build()
            notificationManager.notify(
                Notifications.ID_DOWNLOAD_CHAPTER_PROGRESS,
                notification
            )
        } else {
            notificationManager.cancel(Notifications.ID_DOWNLOAD_CHAPTER_PROGRESS)
        }
    }

    private fun PowerManager.WakeLock.releaseIfNeeded() {
        if (isHeld) release()
    }

    private fun PowerManager.WakeLock.acquireIfNeeded() {
        if (!isHeld) acquire()
    }

    private val icon by lazy {
        BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher)
    }

    private fun getPlaceholderNotification(): Notification {
        return notification(Notifications.CHANNEL_DOWNLOADER_RUNNING) {
            setContentTitle(MR.strings.downloader_running.desc().toString(this@AndroidDownloadService))
            setSmallIcon(R.drawable.ic_round_get_app_24)
        }
    }

    private val progressNotificationBuilder by lazy {
        notificationBuilder(Notifications.CHANNEL_DOWNLOADER_PROGRESS) {
            setLargeIcon(icon)
            setSmallIcon(R.drawable.ic_round_get_app_24)
            setAutoCancel(false)
            setOnlyAlertOnce(true)
            setOngoing(true)
        }
    }
}