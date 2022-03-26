/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.data.download

import ca.gosyer.jui.data.base.WebsocketService
import ca.gosyer.jui.data.download.model.DownloadChapter
import ca.gosyer.jui.data.download.model.DownloadStatus
import ca.gosyer.jui.data.download.model.DownloaderStatus
import ca.gosyer.jui.data.server.Http
import ca.gosyer.jui.data.server.ServerPreferences
import ca.gosyer.jui.data.server.requests.downloadsQuery
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.readText
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.decodeFromString
import me.tatarka.inject.annotations.Inject

@OptIn(DelicateCoroutinesApi::class)
class DownloadService @Inject constructor(
    serverPreferences: ServerPreferences,
    client: Http
) : WebsocketService(serverPreferences, client) {
    override val _status: MutableStateFlow<Status>
        get() = status

    override val query: String
        get() = downloadsQuery()

    override suspend fun onReceived(frame: Frame.Text) {
        val status = json.decodeFromString<DownloadStatus>(frame.readText())
        downloaderStatus.value = status.status
        downloadQueue.value = status.queue
    }

    companion object {
        val status = MutableStateFlow(Status.STARTING)
        val downloadQueue = MutableStateFlow(emptyList<DownloadChapter>())
        val downloaderStatus = MutableStateFlow(DownloaderStatus.Stopped)

        fun registerWatch(mangaId: Long) =
            downloadQueue
                .map {
                    it.filter { it.mangaId == mangaId }
                }
        fun registerWatches(mangaIds: Set<Long>) =
            downloadQueue
                .map {
                    it.filter { it.mangaId in mangaIds }
                }
    }
}
