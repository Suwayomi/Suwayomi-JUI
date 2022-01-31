/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.downloads

import ca.gosyer.data.download.DownloadService
import ca.gosyer.data.models.Chapter
import ca.gosyer.data.server.interactions.ChapterInteractionHandler
import ca.gosyer.data.server.interactions.DownloadInteractionHandler
import ca.gosyer.uicore.vm.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Inject

class DownloadsScreenViewModel @Inject constructor(
    private val downloadService: DownloadService,
    private val downloadsHandler: DownloadInteractionHandler,
    private val chapterHandler: ChapterInteractionHandler,
    standalone: Boolean
) : ViewModel() {
    private val uiScope = if (standalone) {
        MainScope()
    } else null

    override val scope: CoroutineScope
        get() = uiScope ?: super.scope

    val serviceStatus get() = downloadService.status
    val downloaderStatus get() = downloadService.downloaderStatus
    val downloadQueue get() = downloadService.downloadQueue

    fun start() {
        scope.launch {
            downloadsHandler.startDownloading()
        }
    }

    fun pause() {
        scope.launch {
            downloadsHandler.stopDownloading()
        }
    }

    fun clear() {
        scope.launch {
            downloadsHandler.clearDownloadQueue()
        }
    }

    fun stopDownload(chapter: Chapter) {
        scope.launch {
            chapterHandler.stopChapterDownload(chapter)
        }
    }

    fun moveToBottom(chapter: Chapter) {
        scope.launch {
            chapterHandler.stopChapterDownload(chapter)
            chapterHandler.queueChapterDownload(chapter)
        }
    }

    fun restartDownloader() = downloadService.init()
}
