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
import ca.gosyer.jui.domain.chapter.interactor.DeleteChapterDownload
import ca.gosyer.jui.domain.chapter.interactor.QueueChapterDownload
import ca.gosyer.jui.domain.chapter.interactor.StopChapterDownload
import ca.gosyer.jui.domain.chapter.model.Chapter
import ca.gosyer.jui.domain.download.service.DownloadService
import ca.gosyer.jui.domain.updates.interactor.GetRecentUpdates
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
    private val queueChapterDownload: QueueChapterDownload,
    private val stopChapterDownload: StopChapterDownload,
    private val deleteChapterDownload: DeleteChapterDownload,
    private val getRecentUpdates: GetRecentUpdates,
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
        getRecentUpdates.asFlow(page)
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
                log.warn(it) { "Failed to get updates for page $page" }
                if (page > 1) {
                    currentPage.value = page - 1
                }
                _isLoading.value = false
            }
            .collect()
    }

    fun downloadChapter(chapter: Chapter) {
        scope.launch { queueChapterDownload.await(chapter) }
    }

    fun deleteDownloadedChapter(chapter: Chapter) {
        scope.launch {
            _updates
                .firstNotNullOfOrNull { (_, chapters) ->
                    chapters.find {
                        it.chapter.mangaId == chapter.mangaId &&
                                it.chapter.index == chapter.index
                    }
                }
                ?.deleteDownload(deleteChapterDownload)
        }
    }

    fun stopDownloadingChapter(chapter: Chapter) {
        scope.launch {
            _updates
                .firstNotNullOfOrNull { (_, chapters) ->
                    chapters.find {
                        it.chapter.mangaId == chapter.mangaId &&
                                it.chapter.index == chapter.index
                    }
                }
                ?.stopDownloading(stopChapterDownload)
        }
    }

    private companion object {
        private val log = logging()
    }
}
