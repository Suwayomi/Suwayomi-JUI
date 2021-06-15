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
import ca.gosyer.ui.base.vm.ViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

class DownloadsMenuViewModel @Inject constructor(
    private val downloadService: DownloadService,
    private val downloadsHandler: DownloadInteractionHandler,
    private val chapterHandler: ChapterInteractionHandler
) : ViewModel() {
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

    fun stopDownload(chapter: Chapter?) {
        chapter ?: return
        scope.launch {
            chapterHandler.deleteChapterDownload(chapter)
        }
    }

    fun moveToBottom(chapter: Chapter?) {
        chapter ?: return
        scope.launch {
            chapterHandler.deleteChapterDownload(chapter)
            chapterHandler.queueChapterDownload(chapter)
        }
    }
}
