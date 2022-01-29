/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.data.download

import ca.gosyer.core.logging.CKLogger
import ca.gosyer.data.base.WebsocketService
import ca.gosyer.data.download.model.DownloadChapter
import ca.gosyer.data.download.model.DownloadStatus
import ca.gosyer.data.download.model.DownloaderStatus
import ca.gosyer.data.server.Http
import ca.gosyer.data.server.ServerPreferences
import ca.gosyer.data.server.requests.downloadsQuery
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.readText
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.decodeFromString
import me.tatarka.inject.annotations.Inject

@OptIn(DelicateCoroutinesApi::class)
class DownloadService @Inject constructor(
    serverPreferences: ServerPreferences,
    client: Http
) : WebsocketService(serverPreferences, client) {

    private val _downloaderStatus = MutableStateFlow(DownloaderStatus.Stopped)
    val downloaderStatus = _downloaderStatus.asStateFlow()

    private val _downloadQueue = MutableStateFlow(emptyList<DownloadChapter>())
    val downloadQueue = _downloadQueue.asStateFlow()

    private val watching = mutableMapOf<Long, MutableSharedFlow<Pair<Long, List<DownloadChapter>>>>()

    override val query: String
        get() = downloadsQuery()

    override suspend fun onReceived(frame: Frame.Text) {
        val status = json.decodeFromString<DownloadStatus>(frame.readText())
        _downloaderStatus.value = status.status
        _downloadQueue.value = status.queue
        val queue = status.queue.groupBy { it.mangaId }
        watching.forEach { (mangaId, flow) ->
            flow.emit(mangaId to queue[mangaId].orEmpty())
        }
    }

    fun registerWatch(mangaId: Long) =
        MutableSharedFlow<Pair<Long, List<DownloadChapter>>>().also { watching[mangaId] = it }.asSharedFlow()
    fun registerWatches(mangaIds: Set<Long>) =
        mangaIds.map { registerWatch(it) }

    fun removeWatch(mangaId: Long) {
        watching -= mangaId
    }
    fun removeWatches(mangaIds: Set<Long>) {
        watching -= mangaIds
    }

    private companion object : CKLogger({})
}
