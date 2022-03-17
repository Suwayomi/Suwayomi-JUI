/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.updates

import ca.gosyer.core.logging.CKLogger
import ca.gosyer.data.download.DownloadService
import ca.gosyer.data.models.Chapter
import ca.gosyer.data.server.interactions.ChapterInteractionHandler
import ca.gosyer.data.server.interactions.UpdatesInteractionHandler
import ca.gosyer.ui.base.chapter.ChapterDownloadItem
import ca.gosyer.uicore.vm.ContextWrapper
import ca.gosyer.uicore.vm.ViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import me.tatarka.inject.annotations.Inject

class UpdatesScreenViewModel @Inject constructor(
    private val chapterHandler: ChapterInteractionHandler,
    private val updatesHandler: UpdatesInteractionHandler,
    contextWrapper: ContextWrapper
) : ViewModel(contextWrapper) {

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val _updates = MutableStateFlow<List<ChapterDownloadItem>>(emptyList())
    val updates = _updates.asStateFlow()

    private val currentPage = MutableStateFlow(1)
    private val hasNextPage = MutableStateFlow(false)

    private val updatesMutex = Mutex()
    private var downloadServiceJob: Job? = null

    init {
        scope.launch {
            getUpdates()
        }
    }

    fun loadNextPage() {
        scope.launch {
            if (hasNextPage.value && updatesMutex.tryLock()) {
                getUpdates()
                updatesMutex.unlock()
            }
        }
    }

    private suspend fun getUpdates() {
        updatesHandler.getRecentUpdates(currentPage.value)
            .onEach { updates ->
                _updates.value += updates.page.map {
                    ChapterDownloadItem(
                        it.manga,
                        it.chapter
                    )
                }
                downloadServiceJob?.cancel()
                downloadServiceJob = DownloadService.registerWatches(updates.page.map { it.manga.id }.toSet())
                    .onEach { chapters ->
                        _updates.value
                            .forEach {
                                it.updateFrom(chapters)
                            }
                    }
                    .launchIn(scope)

                hasNextPage.value = updates.hasNextPage
                _isLoading.value = false
            }
            .catch {
                info(it) { "Error getting updates" }
                if (currentPage.value > 1) {
                    currentPage.value--
                }
                _isLoading.value = false
            }
            .collect()
    }

    fun downloadChapter(chapter: Chapter) {
        chapterHandler.queueChapterDownload(chapter)
            .catch {
                info(it) { "Error queueing chapter" }
            }
            .launchIn(scope)
    }

    fun deleteDownloadedChapter(chapter: Chapter) {
        updates.value
            .find {
                it.chapter.mangaId == chapter.mangaId &&
                    it.chapter.index == chapter.index
            }
            ?.deleteDownload(chapterHandler)
            ?.catch {
                info(it) { "Error deleting download" }
            }
            ?.launchIn(scope)
    }

    fun stopDownloadingChapter(chapter: Chapter) {
        updates.value
            .find {
                it.chapter.mangaId == chapter.mangaId &&
                    it.chapter.index == chapter.index
            }
            ?.stopDownloading(chapterHandler)
            ?.catch {
                info(it) { "Error stopping download" }
            }
            ?.launchIn(scope)
    }

    private companion object : CKLogger({})
}
