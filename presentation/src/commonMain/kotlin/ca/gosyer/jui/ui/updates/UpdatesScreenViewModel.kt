/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.updates

import ca.gosyer.jui.domain.chapter.interactor.DeleteChapterDownload
import ca.gosyer.jui.domain.chapter.interactor.QueueChapterDownload
import ca.gosyer.jui.domain.chapter.interactor.StopChapterDownload
import ca.gosyer.jui.domain.chapter.model.Chapter
import ca.gosyer.jui.domain.download.service.DownloadService
import ca.gosyer.jui.domain.updates.interactor.GetRecentUpdates
import ca.gosyer.jui.ui.base.chapter.ChapterDownloadItem
import ca.gosyer.jui.uicore.vm.ContextWrapper
import ca.gosyer.jui.uicore.vm.ViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.datetime.Instant
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

    private val _updates = MutableStateFlow<ImmutableList<UpdatesUI>>(persistentListOf())
    val updates = _updates.asStateFlow()

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
                val lastUpdateDate = (_updates.value.lastOrNull() as? UpdatesUI.Item)
                    ?.let {
                        Instant.fromEpochSeconds(it.chapterDownloadItem.chapter.fetchedAt)
                            .toLocalDateTime(TimeZone.currentSystemDefault())
                            .date
                            .toString()
                    }
                val items = updates.page
                    .map {
                        ChapterDownloadItem(
                            it.manga,
                            it.chapter
                        )
                    }
                    .groupBy {
                        Instant.fromEpochSeconds(it.chapter.fetchedAt).toLocalDateTime(TimeZone.currentSystemDefault()).date
                    }
                    .entries
                    .sortedByDescending { it.key.toEpochDays() }
                _updates.value = _updates.value.plus(
                    items
                        .flatMap { (date, updates) ->
                            listOf(UpdatesUI.Header(date.toString())).dropWhile { it.date == lastUpdateDate } +
                                updates
                                    .sortedByDescending { it.chapter.fetchedAt }
                                    .map { UpdatesUI.Item(it) }
                        }
                ).toImmutableList()

                downloadServiceJob?.cancel()
                val mangaIds = _updates.value.filterIsInstance<UpdatesUI.Item>().mapNotNull {
                    it.chapterDownloadItem.manga?.id
                }.toSet()
                downloadServiceJob = DownloadService.registerWatches(mangaIds)
                    .buffer(capacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
                    .onEach { chapters ->
                        _updates.value.filterIsInstance<UpdatesUI.Item>().forEach {
                            it.chapterDownloadItem.updateFrom(chapters)
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
            _updates.value
                .filterIsInstance<UpdatesUI.Item>()
                .find { (chapterDownloadItem) ->
                    chapterDownloadItem.chapter.mangaId == chapter.mangaId &&
                        chapterDownloadItem.chapter.index == chapter.index
                }
                ?.chapterDownloadItem
                ?.deleteDownload(deleteChapterDownload)
        }
    }

    fun stopDownloadingChapter(chapter: Chapter) {
        scope.launch {
            _updates.value
                .filterIsInstance<UpdatesUI.Item>()
                .find { (chapterDownloadItem) ->
                    chapterDownloadItem.chapter.mangaId == chapter.mangaId &&
                        chapterDownloadItem.chapter.index == chapter.index
                }
                ?.chapterDownloadItem
                ?.stopDownloading(stopChapterDownload)
        }
    }

    private companion object {
        private val log = logging()
    }
}

sealed class UpdatesUI {
    data class Item(val chapterDownloadItem: ChapterDownloadItem) : UpdatesUI()
    data class Header(val date: String) : UpdatesUI()
}
