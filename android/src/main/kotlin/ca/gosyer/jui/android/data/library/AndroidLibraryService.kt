/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.android.data.library

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
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
import ca.gosyer.jui.domain.library.model.JobStatus
import ca.gosyer.jui.domain.library.model.UpdateStatus
import ca.gosyer.jui.domain.library.service.LibraryUpdateService
import ca.gosyer.jui.domain.library.service.LibraryUpdateService.Companion.status
import ca.gosyer.jui.i18n.MR
import dev.icerock.moko.resources.desc.desc
import dev.icerock.moko.resources.format
import io.ktor.client.plugins.websocket.ws
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.job
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.lighthousegames.logging.logging

class AndroidLibraryService : Service() {

    companion object {
        private var instance: AndroidLibraryService? = null

        fun start(context: Context, actions: Actions) {
            if (!isRunning() && actions != Actions.STOP) {
                val intent = Intent(context, AndroidLibraryService::class.java).apply {
                    action = actions.name
                }
                ContextCompat.startForegroundService(context, intent)
            }
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, AndroidLibraryService::class.java))
        }

        fun isRunning(): Boolean {
            return instance != null
        }

        private val json = Json {
            ignoreUnknownKeys = true
        }

        private val log = logging()
    }

    private lateinit var ioScope: CoroutineScope

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        ioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        startForeground(Notifications.ID_LIBRARY_UPDATES, placeholderNotification)
        status.value = Status.STARTING
    }

    override fun onDestroy() {
        ioScope.cancel()
        status.value = Status.STOPPED
        notificationManager.cancel(Notifications.ID_LIBRARY_UPDATES)
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
            log.info { "using an intent with action $action" }
            when (action) {
                Actions.START.name,
                Actions.RESTART.name -> startWebsocket()
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
                        client.ws(
                            host = serverUrl.host,
                            port = serverUrl.port,
                            path = serverUrl.encodedPath + "/api/v1/update"
                        ) {
                            errorConnectionCount = 0
                            status.value = Status.RUNNING
                            send(Frame.Text("STATUS"))

                            incoming.receiveAsFlow()
                                .filterIsInstance<Frame.Text>()
                                .map { json.decodeFromString<UpdateStatus>(it.readText()) }
                                .distinctUntilChanged()
                                .drop(1)
                                .mapLatest(::onReceived)
                                .catch {
                                    log.warn(it) { "Error running library update" }
                                }
                                .collect()
                        }
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

    private fun onReceived(status: UpdateStatus) {
        LibraryUpdateService.updateStatus.value = status

        val complete = status.statusMap[JobStatus.COMPLETE]?.size ?: 0
        val failed = status.statusMap[JobStatus.FAILED]?.size ?: 0
        val running = status.statusMap[JobStatus.RUNNING]?.size ?: 0
        val pending = status.statusMap[JobStatus.PENDING]?.size ?: 0
        val total = complete + failed + running + pending
        val current = complete + failed
        if (current != total) {
            val notification = with(progressNotificationBuilder) {
                val updatingText = status.statusMap[JobStatus.RUNNING]
                    ?.joinToString("\n") { it.title.chop(40) }
                setContentTitle(
                    MR.strings.notification_updating
                        .format(current, total)
                        .toString(this@AndroidLibraryService)
                )
                setStyle(NotificationCompat.BigTextStyle().bigText(updatingText))
                setProgress(total, current, false)
            }.build()
            notificationManager.notify(
                Notifications.ID_LIBRARY_PROGRESS,
                notification
            )
        } else {
            notificationManager.cancel(Notifications.ID_LIBRARY_PROGRESS)
        }
    }

    private val placeholderNotification by lazy {
        notification(Notifications.CHANNEL_LIBRARY_UPDATES) {
            setContentTitle(MR.strings.library_updater_running.desc().toString(this@AndroidLibraryService))
            setSmallIcon(R.drawable.ic_round_get_app_24)
        }
    }
    private val progressNotificationBuilder by lazy {
        notificationBuilder(Notifications.CHANNEL_LIBRARY_PROGRESS) {
            setSmallIcon(R.drawable.ic_round_get_app_24)
            setAutoCancel(false)
            setOnlyAlertOnce(true)
            setOngoing(true)
        }
    }
}
