/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.updates

import ca.gosyer.data.download.DownloadService
import ca.gosyer.data.models.Chapter
import ca.gosyer.data.server.interactions.ChapterInteractionHandler
import ca.gosyer.data.server.interactions.UpdatesInteractionHandler
import ca.gosyer.ui.base.components.ChapterDownloadItem
import ca.gosyer.ui.base.vm.ViewModel
import ca.gosyer.util.lang.throwIfCancellation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

class UpdatesMenuViewModel @Inject constructor(
    private val chapterHandler: ChapterInteractionHandler,
    private val updatesHandler: UpdatesInteractionHandler,
    private val downloadService: DownloadService
) : ViewModel() {

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private var mangaIds: Set<Long> = emptySet()

    private val _updates = MutableStateFlow<List<ChapterDownloadItem>>(emptyList())
    val updates = _updates.asStateFlow()

    init {
        scope.launch {
            try {
                val updates = updatesHandler.getRecentUpdates()
                mangaIds = updates.map { it.manga.id }.toSet()

                _updates.value = updates.map {
                    ChapterDownloadItem(
                        it.manga,
                        it.chapter
                    )
                }
                downloadService.registerWatches(mangaIds).merge()
                    .onEach { (mangaId, chapters) ->
                        _updates.value.filter { it.chapter.mangaId == mangaId }
                            .forEach {
                                it.updateFrom(chapters)
                            }
                    }
                    .launchIn(scope)
            } catch (e: Exception) {
                e.throwIfCancellation()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun downloadChapter(chapter: Chapter) {
        scope.launch {
            chapterHandler.queueChapterDownload(chapter)
        }
    }

    fun deleteDownloadedChapter(chapter: Chapter) {
        scope.launch {
            updates.value.find {
                it.chapter.mangaId == chapter.mangaId &&
                    it.chapter.index == chapter.index
            }?.deleteDownload(chapterHandler)
        }
    }

    fun stopDownloadingChapter(chapter: Chapter) {
        scope.launch {
            updates.value.find {
                it.chapter.mangaId == chapter.mangaId &&
                    it.chapter.index == chapter.index
            }?.stopDownloading(chapterHandler)
        }
    }

    override fun onDestroy() {
        downloadService.removeWatches(mangaIds)
    }
}
