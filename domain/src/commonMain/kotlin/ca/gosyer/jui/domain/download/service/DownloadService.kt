/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.domain.download.service

import ca.gosyer.jui.domain.base.WebsocketService
import ca.gosyer.jui.domain.download.model.DownloadQueueItem
import ca.gosyer.jui.domain.download.model.DownloadUpdateType
import ca.gosyer.jui.domain.download.model.DownloaderState
import com.diamondedge.logging.logging
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import me.tatarka.inject.annotations.Inject

@Inject
class DownloadService(
    private val downloadRepository: DownloadRepository,
) {
    private val log = logging()

    fun getSubscription(): Flow<Unit> {
        return downloadRepository.downloadSubscription()
            .onStart {
                log.info { "Starting download status subscription" }
                status.value = WebsocketService.Status.STARTING
            }
            .catch { error ->
                log.error(error) { "Error in download status subscription" }
                status.value = WebsocketService.Status.STOPPED
            }
            .map { updates ->
                status.value = WebsocketService.Status.RUNNING
                if (updates.omittedUpdates) {
                    log.info { "Omitted updates detected, fetching fresh download status" }
                    fetchDownloadStatus()
                    return@map
                }
                if (updates.initial != null) {
                    downloadQueue.value = updates.initial
                }
                downloaderStatus.value = updates.state
                updates.updates?.forEach { update ->
                    when (update.type) {
                        DownloadUpdateType.QUEUED -> {
                            update.download?.let { download ->
                                downloadQueue.update {
                                    it.toMutableList().apply {
                                        add(download.position.coerceAtMost(it.size), download)
                                    }
                                }
                            }
                        }
                        DownloadUpdateType.DEQUEUED -> {
                            downloadQueue.update {
                                it.filter { it.chapter.id != update.download?.chapter?.id }
                            }
                        }
                        DownloadUpdateType.PAUSED -> {
                            downloaderStatus.value = DownloaderState.STOPPED
                        }
                        DownloadUpdateType.STOPPED -> {
                            downloaderStatus.value = DownloaderState.STOPPED
                        }
                        DownloadUpdateType.ERROR -> {
                            update.download?.let { download ->
                                downloadQueue.update {
                                    it.map { chapter ->
                                        if (chapter.chapter.id == download.chapter.id) {
                                            chapter.copy(state = download.state)
                                        } else {
                                            chapter
                                        }
                                    }
                                }
                            }
                        }
                        DownloadUpdateType.PROGRESS -> {
                            update.download?.let { download ->
                                downloadQueue.update {
                                    it.map { chapter ->
                                        if (chapter.chapter.id == download.chapter.id) {
                                            chapter.copy(progress = download.progress)
                                        } else {
                                            chapter
                                        }
                                    }
                                }
                            }
                        }
                        DownloadUpdateType.FINISHED -> {
                            downloadQueue.update {
                                it.filter { it.chapter.id != update.download?.chapter?.id }
                            }
                        }
                        DownloadUpdateType.POSITION -> {
                            update.download?.let { download ->
                                downloadQueue.update {
                                    val index = it.indexOfFirst { it.chapter.id == download.chapter.id }
                                    if (index != -1) {
                                        it.toMutableList().apply {
                                            removeAt(index)
                                            add(download.position.coerceAtMost(it.size), download)
                                        }.toList()
                                    } else it
                                }

                            }
                        }
                        null -> {
                            // todo Handle null case
                        }
                    }
                }
            }
    }

    private suspend fun fetchDownloadStatus() {
        val status = downloadRepository.downloadStatus().firstOrNull()
        if (status != null) {
            downloadQueue.value = status.queue
            downloaderStatus.value = status.status
        }
    }

    companion object {
        val status = MutableStateFlow(WebsocketService.Status.STARTING)
        val downloadQueue = MutableStateFlow(emptyList<DownloadQueueItem>())
        val downloaderStatus = MutableStateFlow(DownloaderState.STOPPED)

        fun registerWatch(mangaId: Long) =
            downloadQueue
                .map {
                    it.filter { it.manga.id == mangaId }
                }

        fun registerWatches(mangaIds: Set<Long>) =
            downloadQueue
                .map {
                    it.filter { it.manga.id in mangaIds }
                }
    }
}
