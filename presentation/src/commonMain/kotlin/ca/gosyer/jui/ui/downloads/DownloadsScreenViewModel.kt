/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.downloads

import ca.gosyer.jui.domain.base.WebsocketService.Actions
import ca.gosyer.jui.domain.chapter.interactor.QueueChapterDownload
import ca.gosyer.jui.domain.chapter.interactor.StopChapterDownload
import ca.gosyer.jui.domain.chapter.model.Chapter
import ca.gosyer.jui.domain.download.interactor.ClearDownloadQueue
import ca.gosyer.jui.domain.download.interactor.StartDownloading
import ca.gosyer.jui.domain.download.interactor.StopDownloading
import ca.gosyer.jui.domain.download.service.DownloadService
import ca.gosyer.jui.uicore.vm.ContextWrapper
import ca.gosyer.jui.uicore.vm.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Inject
import org.lighthousegames.logging.logging

class DownloadsScreenViewModel @Inject constructor(
    private val downloadService: DownloadService,
    private val startDownloading: StartDownloading,
    private val stopDownloading: StopDownloading,
    private val clearDownloadQueue: ClearDownloadQueue,
    private val queueChapterDownload: QueueChapterDownload,
    private val stopChapterDownload: StopChapterDownload,
    private val contextWrapper: ContextWrapper,
    standalone: Boolean
) : ViewModel(contextWrapper) {
    private val uiScope = if (standalone) {
        MainScope()
    } else null

    override val scope: CoroutineScope
        get() = uiScope ?: super.scope

    val serviceStatus get() = DownloadService.status.asStateFlow()
    val downloaderStatus get() = DownloadService.downloaderStatus.asStateFlow()
    val downloadQueue get() = DownloadService.downloadQueue.asStateFlow()

    fun start() {
        scope.launch { startDownloading.await() }
    }

    fun pause() {
        scope.launch { stopDownloading.await() }
    }

    fun clear() {
        scope.launch { clearDownloadQueue.await() }
    }

    fun stopDownload(chapter: Chapter) {
        scope.launch { stopChapterDownload.await(chapter) }
    }

    fun moveToBottom(chapter: Chapter) {
        scope.launch {
            stopChapterDownload.await(chapter)
            queueChapterDownload.await(chapter)
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
