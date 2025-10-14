/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.android.data.download

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.content.ContextCompat
import ca.gosyer.jui.android.AppComponent
import ca.gosyer.jui.android.R
import ca.gosyer.jui.android.data.notification.Notifications
import ca.gosyer.jui.android.util.notification
import ca.gosyer.jui.android.util.notificationBuilder
import ca.gosyer.jui.android.util.notificationManager
import ca.gosyer.jui.core.lang.chop
import ca.gosyer.jui.core.lang.throwIfCancellation
import ca.gosyer.jui.core.prefs.getAsFlow
import ca.gosyer.jui.domain.base.WebsocketService.Actions
import ca.gosyer.jui.domain.base.WebsocketService.Status
import ca.gosyer.jui.domain.download.model.DownloadState
import ca.gosyer.jui.domain.download.service.DownloadService
import ca.gosyer.jui.domain.download.service.DownloadService.Companion.status
import ca.gosyer.jui.i18n.MR
import com.diamondedge.logging.logging
import dev.icerock.moko.resources.desc.desc
import dev.icerock.moko.resources.format
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.job
import kotlinx.serialization.json.Json
import java.util.regex.Pattern

class AndroidDownloadService : Service() {
    companion object {
        private var instance: AndroidDownloadService? = null

        fun start(
            context: Context,
            actions: Actions,
        ) {
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

        fun isRunning(): Boolean = instance != null

        private val json = Json {
            ignoreUnknownKeys = true
        }

        private val log = logging()
    }

    private lateinit var ioScope: CoroutineScope

    override fun onBind(intent: Intent): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        ioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        startForeground(Notifications.ID_DOWNLOADER_RUNNING, placeholderNotification)
        status.value = Status.STARTING
    }

    override fun onDestroy() {
        ioScope.cancel()
        status.value = Status.STOPPED
        notificationManager.cancel(Notifications.ID_DOWNLOADER_RUNNING)
        if (instance == this) {
            instance = null
        }
        super.onDestroy()
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int,
    ): Int {
        instance = this
        ioScope.coroutineContext.job.cancelChildren()

        if (intent != null) {
            val action = intent.action
            log.info { "using an intent with action $action" }
            when (action) {
                Actions.START.name,
                Actions.RESTART.name,
                -> startWebsocket()

                Actions.STOP.name -> stopSelf()

                else -> log.info { "This should never happen. No action in the received intent" }
            }
        } else {
            log.info { "with a null intent. It has been probably restarted by the system." }
            startWebsocket()
        }

        return START_STICKY
    }

    private fun startWebsocket() {
        ioScope.coroutineContext.job.cancelChildren()
        ioScope.coroutineContext.job.invokeOnCompletion {
            stopSelf()
        }

        val appComponent = AppComponent.getInstance(applicationContext)
        val client = appComponent.http

        var errorConnectionCount = 0

        appComponent
            .serverPreferences
            .serverUrl()
            .getAsFlow()
            .mapLatest { serverUrl ->
                status.value = Status.STARTING
                while (true) {
                    if (errorConnectionCount > 3) {
                        status.value = Status.STOPPED
                        throw CancellationException()
                    }
                    runCatching {
                        appComponent.downloadService
                            .getSubscription()
                            .onEach {
                                onReceived()
                            }
                            .collect()
                    }.throwIfCancellation().isFailure.let {
                        status.value = Status.STARTING
                        if (it) errorConnectionCount++
                    }
                }
            }
            .catch {
                status.value = Status.STOPPED
                log.warn(it) { "Error while running websocket service" }
                stopSelf()
            }
            .launchIn(ioScope)
    }

    private fun onReceived() {
        val downloadingChapter = DownloadService.downloadQueue.value.lastOrNull { it.state == DownloadState.DOWNLOADING }
        if (downloadingChapter != null) {
            val notification = with(progressNotificationBuilder) {
                val max = downloadingChapter.chapter.pageCount
                val current = (max * downloadingChapter.progress).toInt().coerceIn(0, max)
                setProgress(max, current, false)

                val title = downloadingChapter.manga.title.chop(15)
                val quotedTitle = Pattern.quote(title)
                val chapter = downloadingChapter.chapter.name.replaceFirst(
                    "$quotedTitle[\\s]*[-]*[\\s]*".toRegex(RegexOption.IGNORE_CASE),
                    "",
                )
                setContentTitle("$title - $chapter".chop(30))

                setContentText(
                    MR.strings.chapter_downloading_progress
                        .format(
                            current,
                            max,
                        )
                        .toString(this@AndroidDownloadService),
                )
            }.build()
            notificationManager.notify(
                Notifications.ID_DOWNLOADER_DOWNLOADING,
                notification,
            )
        } else {
            notificationManager.cancel(Notifications.ID_DOWNLOADER_DOWNLOADING)
        }
    }

    private val placeholderNotification by lazy {
        notification(Notifications.CHANNEL_DOWNLOADER_RUNNING) {
            setContentTitle(MR.strings.downloader_running.desc().toString(this@AndroidDownloadService))
            setSmallIcon(R.drawable.ic_round_get_app_24)
        }
    }
    private val progressNotificationBuilder by lazy {
        notificationBuilder(Notifications.CHANNEL_DOWNLOADER_DOWNLOADING) {
            setSmallIcon(R.drawable.ic_round_get_app_24)
            setAutoCancel(false)
            setOnlyAlertOnce(true)
            setOngoing(true)
        }
    }
}
