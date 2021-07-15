/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.data.download

import ca.gosyer.BuildConfig
import ca.gosyer.data.download.model.DownloadChapter
import ca.gosyer.data.download.model.DownloadStatus
import ca.gosyer.data.download.model.DownloaderStatus
import ca.gosyer.data.server.Http
import ca.gosyer.data.server.ServerPreferences
import ca.gosyer.data.server.requests.downloadsQuery
import ca.gosyer.util.lang.throwIfCancellation
import ca.gosyer.util.system.CKLogger
import io.ktor.client.features.websocket.ws
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.readText
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import javax.inject.Inject

@OptIn(DelicateCoroutinesApi::class)
class DownloadService @Inject constructor(
    val serverPreferences: ServerPreferences,
    val client: Http
) {
    private val json = Json {
        ignoreUnknownKeys = !BuildConfig.DEBUG
    }
    private val _status = MutableStateFlow(Status.STARTING)
    val status = _status.asStateFlow()

    private val serverUrl = serverPreferences.serverUrl().stateIn(GlobalScope)
    private val _downloaderStatus = MutableStateFlow(DownloaderStatus.Stopped)
    val downloaderStatus = _downloaderStatus.asStateFlow()

    private val _downloadQueue = MutableStateFlow(emptyList<DownloadChapter>())
    val downloadQueue = _downloadQueue.asStateFlow()

    private val watching = mutableMapOf<Long, MutableSharedFlow<List<DownloadChapter>>>()
    private var errorConnectionCount = 0

    private var job: Job? = null
    init {
        init()
    }

    fun init() {
        errorConnectionCount = 0
        job?.cancel()
        job = serverUrl.mapLatest { serverUrl ->
            _status.value = Status.STARTING
            while (true) {
                if (errorConnectionCount > 3) {
                    _status.value = Status.STOPPED
                    throw CancellationException()
                }
                runCatching {
                    client.ws(
                        host = serverUrl.substringAfter("://"),
                        path = downloadsQuery()
                    ) {
                        _status.value = Status.RUNNING
                        send(Frame.Text("STATUS"))

                        while (true) {
                            val frame = incoming.receive()
                            runCatching {
                                frame as Frame.Text
                                val status = json.decodeFromString<DownloadStatus>(frame.readText())
                                _downloaderStatus.value = status.status
                                _downloadQueue.value = status.queue
                                val queue = status.queue.groupBy { it.mangaId }
                                watching.forEach { (mangaId, flow) ->
                                    flow.emit(queue[mangaId].orEmpty())
                                }
                            }.throwIfCancellation()
                        }
                    }
                }.throwIfCancellation().isFailure.let {
                    _status.value = Status.STARTING
                    if (it) errorConnectionCount++
                }
            }
        }.catch {
            _status.value = Status.STOPPED
            error(it) { "Error while running downloader" }
            throw it
        }.launchIn(GlobalScope)
    }

    fun registerWatch(mangaId: Long) =
        MutableSharedFlow<List<DownloadChapter>>().also { watching[mangaId] = it }.asSharedFlow()

    fun removeWatch(mangaId: Long) {
        watching.remove(mangaId)
    }

    enum class Status {
        STARTING,
        RUNNING,
        STOPPED
    }

    private companion object : CKLogger({})
}
