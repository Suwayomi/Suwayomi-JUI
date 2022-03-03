/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.downloads

import ca.gosyer.core.logging.CKLogger
import ca.gosyer.data.download.DownloadService
import ca.gosyer.data.models.Chapter
import ca.gosyer.data.server.interactions.ChapterInteractionHandler
import ca.gosyer.data.server.interactions.DownloadInteractionHandler
import ca.gosyer.uicore.vm.ContextWrapper
import ca.gosyer.uicore.vm.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.tatarka.inject.annotations.Inject

class DownloadsScreenViewModel @Inject constructor(
    private val downloadService: DownloadService,
    private val downloadsHandler: DownloadInteractionHandler,
    private val chapterHandler: ChapterInteractionHandler,
    contextWrapper: ContextWrapper,
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
        downloadsHandler.startDownloading()
            .catch {
                info(it) { "Error starting download" }
            }
            .launchIn(scope)
    }

    fun pause() {
        downloadsHandler.stopDownloading()
            .catch {
                info(it) { "Error stopping download" }
            }
            .launchIn(scope)
    }

    fun clear() {
        downloadsHandler.clearDownloadQueue()
            .catch {
                info(it) { "Error clearing download" }
            }
            .launchIn(scope)
    }

    fun stopDownload(chapter: Chapter) {
        chapterHandler.stopChapterDownload(chapter)
            .catch {
                info(it) { "Error stop chapter download" }
            }
            .launchIn(scope)
    }

    fun moveToBottom(chapter: Chapter) {
        chapterHandler.stopChapterDownload(chapter)
            .onEach {
                chapterHandler.queueChapterDownload(chapter)
                    .catch {
                        info(it) { "Error adding download" }
                    }
                    .collect()
            }
            .catch {
                info(it) { "Error stop chapter download" }
            }
            .launchIn(scope)
    }

    fun restartDownloader() = downloadService.init()

    private companion object : CKLogger({})
}
