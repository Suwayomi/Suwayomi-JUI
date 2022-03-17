/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.data.base

import ca.gosyer.core.lang.throwIfCancellation
import ca.gosyer.core.logging.CKLogger
import ca.gosyer.data.build.BuildKonfig
import ca.gosyer.data.server.Http
import ca.gosyer.data.server.ServerPreferences
import io.ktor.client.features.websocket.ws
import io.ktor.http.cio.websocket.Frame
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.serialization.json.Json

@OptIn(DelicateCoroutinesApi::class)
abstract class WebsocketService(
    protected val serverPreferences: ServerPreferences,
    protected val client: Http
) {
    protected val json = Json {
        ignoreUnknownKeys = !BuildKonfig.DEBUG
    }
    protected abstract val _status: MutableStateFlow<Status>

    protected val serverUrl = serverPreferences.serverUrl().stateIn(GlobalScope)

    private var errorConnectionCount = 0

    private var job: Job? = null

    fun init() {
        errorConnectionCount = 0
        job?.cancel()
        job = serverUrl
            .mapLatest { serverUrl ->
                _status.value = Status.STARTING
                while (true) {
                    if (errorConnectionCount > 3) {
                        _status.value = Status.STOPPED
                        throw CancellationException("Finish")
                    }
                    runCatching {
                        client.ws(
                            host = serverUrl.host,
                            port = serverUrl.port,
                            path = serverUrl.encodedPath + query
                        ) {
                            errorConnectionCount = 0
                            _status.value = Status.RUNNING
                            send(Frame.Text("STATUS"))

                            incoming.receiveAsFlow()
                                .filterIsInstance<Frame.Text>()
                                .mapLatest(::onReceived)
                                .catch {
                                    info(it) { "Error running websocket" }
                                }
                                .collect()
                        }
                    }.throwIfCancellation().isFailure.let {
                        _status.value = Status.STARTING
                        if (it) errorConnectionCount++
                    }
                }
            }
            .catch {
                _status.value = Status.STOPPED
                error(it) { "Error while running websocket service" }
            }
            .launchIn(GlobalScope)
    }

    abstract val query: String

    abstract suspend fun onReceived(frame: Frame.Text)

    enum class Status {
        STARTING,
        RUNNING,
        STOPPED
    }

    enum class Actions {
        STOP,
        START,
        RESTART
    }

    private companion object : CKLogger({})
}
