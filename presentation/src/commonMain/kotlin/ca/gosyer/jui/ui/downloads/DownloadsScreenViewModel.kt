/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.downloads

import ca.gosyer.jui.domain.base.WebsocketService.Actions
import ca.gosyer.jui.domain.chapter.model.Chapter
import ca.gosyer.jui.domain.download.interactor.ClearDownloadQueue
import ca.gosyer.jui.domain.download.interactor.QueueChapterDownload
import ca.gosyer.jui.domain.download.interactor.ReorderChapterDownload
import ca.gosyer.jui.domain.download.interactor.StartDownloading
import ca.gosyer.jui.domain.download.interactor.StopChapterDownload
import ca.gosyer.jui.domain.download.interactor.StopDownloading
import ca.gosyer.jui.domain.download.service.DownloadService
import ca.gosyer.jui.uicore.vm.ContextWrapper
import ca.gosyer.jui.uicore.vm.ViewModel
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject
import com.diamondedge.logging.logging

@Inject
class DownloadsScreenViewModel(
    private val downloadService: DownloadService,
    private val startDownloading: StartDownloading,
    private val stopDownloading: StopDownloading,
    private val clearDownloadQueue: ClearDownloadQueue,
    private val queueChapterDownload: QueueChapterDownload,
    private val stopChapterDownload: StopChapterDownload,
    private val reorderChapterDownload: ReorderChapterDownload,
    private val contextWrapper: ContextWrapper,
    @Assisted standalone: Boolean,
) : ViewModel(contextWrapper) {
    private val uiScope = if (standalone) {
        MainScope()
    } else {
        null
    }

    override val scope: CoroutineScope
        get() = uiScope ?: super.scope

    val serviceStatus = DownloadService.status.asStateFlow()
    val downloaderStatus = DownloadService.downloaderStatus.asStateFlow()
    val downloadQueue = DownloadService.downloadQueue.map { it.toImmutableList() }
        .stateIn(scope, SharingStarted.Eagerly, persistentListOf())

    fun start() {
        scope.launch { startDownloading.await(onError = { toast(it.message.orEmpty()) }) }
    }

    fun pause() {
        scope.launch { stopDownloading.await(onError = { toast(it.message.orEmpty()) }) }
    }

    fun clear() {
        scope.launch { clearDownloadQueue.await(onError = { toast(it.message.orEmpty()) }) }
    }

    fun stopDownload(chapter: Chapter) {
        scope.launch { stopChapterDownload.await(chapter.id, onError = { toast(it.message.orEmpty()) }) }
    }

    fun moveUp(chapter: Chapter) {
        scope.launch {
            val index =
                downloadQueue.value.indexOfFirst { it.mangaId == chapter.mangaId && it.chapterIndex == chapter.index }
            if (index == -1 || index <= 0) return@launch
            reorderChapterDownload.await(chapter.id, index - 1, onError = { toast(it.message.orEmpty()) })
        }
    }

    fun moveDown(chapter: Chapter) {
        scope.launch {
            val index =
                downloadQueue.value.indexOfFirst { it.mangaId == chapter.mangaId && it.chapterIndex == chapter.index }
            if (index == -1 || index >= downloadQueue.value.lastIndex) return@launch
            reorderChapterDownload.await(chapter.id, index + 1, onError = { toast(it.message.orEmpty()) })
        }
    }

    fun moveToTop(chapter: Chapter) {
        scope.launch {
            reorderChapterDownload.await(chapter.id, 0, onError = { toast(it.message.orEmpty()) })
        }
    }

    fun moveToBottom(chapter: Chapter) {
        scope.launch {
            reorderChapterDownload.await(
                chapter.id,
                downloadQueue.value.lastIndex,
                onError = { toast(it.message.orEmpty()) },
            )
        }
    }

    fun restartDownloader() = startDownloadService(contextWrapper, downloadService, Actions.RESTART)

    override fun onDispose() {
        super.onDispose()
        uiScope?.cancel()
    }

    private companion object {
        private val log = logging()
    }
}
