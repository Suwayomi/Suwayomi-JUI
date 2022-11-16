/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.updates

import ca.gosyer.jui.domain.chapter.interactor.BatchUpdateChapter
import ca.gosyer.jui.domain.chapter.interactor.DeleteChapterDownload
import ca.gosyer.jui.domain.chapter.model.Chapter
import ca.gosyer.jui.domain.download.interactor.BatchChapterDownload
import ca.gosyer.jui.domain.download.interactor.QueueChapterDownload
import ca.gosyer.jui.domain.download.interactor.StopChapterDownload
import ca.gosyer.jui.domain.download.service.DownloadService
import ca.gosyer.jui.domain.updates.interactor.GetRecentUpdates
import ca.gosyer.jui.domain.updates.interactor.UpdateLibrary
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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
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
    private val batchUpdateChapter: BatchUpdateChapter,
    private val batchChapterDownload: BatchChapterDownload,
    private val updateLibrary: UpdateLibrary,
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

    private val _selectedIds = MutableStateFlow<ImmutableList<Long>>(persistentListOf())
    val selectedItems = combine(updates, _selectedIds) { updates, selecteditems ->
        updates.filterIsInstance<UpdatesUI.Item>()
            .filter { it.chapterDownloadItem.isSelected(selecteditems) }
            .map { it.chapterDownloadItem }
            .toImmutableList()
    }.stateIn(scope, SharingStarted.Eagerly, persistentListOf())

    val inActionMode = _selectedIds.map { it.isNotEmpty() }
        .stateIn(scope, SharingStarted.Eagerly, false)

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
                toast(it.message.orEmpty())
                log.warn(it) { "Failed to get updates for page $page" }
                if (page > 1) {
                    currentPage.value = page - 1
                }
                _isLoading.value = false
            }
            .collect()
    }

    private fun setRead(chapterIds: List<Long>, read: Boolean) {
        scope.launch {
            batchUpdateChapter.await(chapterIds, isRead = read, onError = { toast(it.message.orEmpty()) })
            _selectedIds.value = persistentListOf()
        }
    }
    fun markRead(id: Long?) = setRead(listOfNotNull(id).ifEmpty { _selectedIds.value }, true)
    fun markUnread(id: Long?) = setRead(listOfNotNull(id).ifEmpty { _selectedIds.value }, false)

    private fun setBookmarked(chapterIds: List<Long>, bookmark: Boolean) {
        scope.launch {
            batchUpdateChapter.await(chapterIds, isBookmarked = bookmark, onError = { toast(it.message.orEmpty()) })
            _selectedIds.value = persistentListOf()
        }
    }
    fun bookmarkChapter(id: Long?) = setBookmarked(listOfNotNull(id).ifEmpty { _selectedIds.value }, true)
    fun unBookmarkChapter(id: Long?) = setBookmarked(listOfNotNull(id).ifEmpty { _selectedIds.value }, false)

    fun downloadChapter(chapter: Chapter?) {
        scope.launch {
            if (chapter == null) {
                val selectedIds = _selectedIds.value
                batchChapterDownload.await(selectedIds, onError = { toast(it.message.orEmpty()) })
                _selectedIds.value = persistentListOf()
                return@launch
            }
            queueChapterDownload.await(chapter, onError = { toast(it.message.orEmpty()) })
        }
    }

    fun deleteDownloadedChapter(chapter: Chapter?) {
        scope.launch {
            if (chapter == null) {
                val selectedIds = _selectedIds.value
                batchUpdateChapter.await(selectedIds, delete = true, onError = { toast(it.message.orEmpty()) })
                selectedItems.value.forEach {
                    it.setNotDownloaded()
                }
                _selectedIds.value = persistentListOf()
                return@launch
            }
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


    fun selectAll() {
        scope.launch {
            _selectedIds.value = updates.value.filterIsInstance<UpdatesUI.Item>()
                .map { it.chapterDownloadItem.chapter.id }
                .toImmutableList()
        }
    }

    fun invertSelection() {
        scope.launch {
            _selectedIds.value = updates.value.filterIsInstance<UpdatesUI.Item>()
                .map { it.chapterDownloadItem.chapter.id }
                .minus(_selectedIds.value)
                .toImmutableList()
        }
    }

    fun selectChapter(id: Long) {
        scope.launch {
            _selectedIds.value = _selectedIds.value.plus(id).toImmutableList()
        }
    }
    fun unselectChapter(id: Long) {
        scope.launch {
            _selectedIds.value = _selectedIds.value.minus(id).toImmutableList()
        }
    }

    fun clearSelection() {
        scope.launch {
            _selectedIds.value = persistentListOf()
        }
    }

    fun updateLibrary() {
        scope.launch { updateLibrary.await(onError = { toast(it.message.orEmpty()) }) }
    }

    private companion object {
        private val log = logging()
    }
}

sealed class UpdatesUI {
    data class Item(val chapterDownloadItem: ChapterDownloadItem) : UpdatesUI()
    data class Header(val date: String) : UpdatesUI()
}
