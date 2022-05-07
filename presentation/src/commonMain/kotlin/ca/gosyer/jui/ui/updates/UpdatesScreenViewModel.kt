/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.updates

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.snapshots.SnapshotStateList
import ca.gosyer.jui.data.download.DownloadService
import ca.gosyer.jui.data.models.Chapter
import ca.gosyer.jui.data.server.interactions.ChapterInteractionHandler
import ca.gosyer.jui.data.server.interactions.UpdatesInteractionHandler
import ca.gosyer.jui.ui.base.chapter.ChapterDownloadItem
import ca.gosyer.jui.uicore.vm.ContextWrapper
import ca.gosyer.jui.uicore.vm.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import me.tatarka.inject.annotations.Inject
import org.lighthousegames.logging.logging

class UpdatesScreenViewModel @Inject constructor(
    private val chapterHandler: ChapterInteractionHandler,
    private val updatesHandler: UpdatesInteractionHandler,
    contextWrapper: ContextWrapper
) : ViewModel(contextWrapper) {

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val _updates = mutableStateMapOf<LocalDate, SnapshotStateList<ChapterDownloadItem>>()
    val updates = snapshotFlow { _updates.toList().sortedByDescending { it.first } }
        .stateIn(scope, SharingStarted.Eagerly, emptyList())

    private val currentPage = MutableStateFlow(1)
    private val hasNextPage = MutableStateFlow(false)

    private val updatesMutex = Mutex()
    private var downloadServiceJob: Job? = null

    init {
        scope.launch(Dispatchers.Default) {
            getUpdates(currentPage.value)
        }
    }

    fun loadNextPage() {
        scope.launch(Dispatchers.Default) {
            if (hasNextPage.value && updatesMutex.tryLock()) {
                currentPage.value++
                getUpdates(currentPage.value)
                updatesMutex.unlock()
            }
        }
    }

    private suspend fun getUpdates(page: Int) {
        updatesHandler.getRecentUpdates(page)
            .onEach { updates ->
                updates.page
                    .map {
                        ChapterDownloadItem(
                            it.manga,
                            it.chapter
                        )
                    }
                    .groupBy {
                        Instant.fromEpochSeconds(it.chapter.fetchedAt).toLocalDateTime(TimeZone.currentSystemDefault()).date
                    }.forEach { (date, chapters) ->
                        val list = _updates.getOrPut(date, ::mutableStateListOf)
                        list += chapters
                        list.sortByDescending { it.chapter.fetchedAt }
                    }

                downloadServiceJob?.cancel()
                val mangaIds = _updates.values.flatMap { items ->
                    items.mapNotNull { it.manga?.id }
                }.toSet()
                downloadServiceJob = DownloadService.registerWatches(mangaIds)
                    .onEach { chapters ->
                        _updates.forEach { (_, updates) ->
                            updates.forEach {
                                it.updateFrom(chapters)
                            }
                        }
                    }
                    .launchIn(scope)

                hasNextPage.value = updates.hasNextPage
                _isLoading.value = false
            }
            .catch {
                log.warn(it) { "Error getting updates" }
                if (page > 1) {
                    currentPage.value = page - 1
                }
                _isLoading.value = false
            }
            .collect()
    }

    fun downloadChapter(chapter: Chapter) {
        chapterHandler.queueChapterDownload(chapter)
            .catch {
                log.warn(it) { "Error queueing chapter" }
            }
            .launchIn(scope)
    }

    fun deleteDownloadedChapter(chapter: Chapter) {
        _updates
            .firstNotNullOfOrNull { (_, chapters) ->
                chapters.find {
                    it.chapter.mangaId == chapter.mangaId &&
                        it.chapter.index == chapter.index
                }
            }
            ?.deleteDownload(chapterHandler)
            ?.catch {
                log.warn(it) { "Error deleting download" }
            }
            ?.launchIn(scope)
    }

    fun stopDownloadingChapter(chapter: Chapter) {
        _updates
            .firstNotNullOfOrNull { (_, chapters) ->
                chapters.find {
                    it.chapter.mangaId == chapter.mangaId &&
                        it.chapter.index == chapter.index
                }
            }
            ?.stopDownloading(chapterHandler)
            ?.catch {
                log.warn(it) { "Error stopping download" }
            }
            ?.launchIn(scope)
    }

    private companion object {
        private val log = logging()
    }
}
