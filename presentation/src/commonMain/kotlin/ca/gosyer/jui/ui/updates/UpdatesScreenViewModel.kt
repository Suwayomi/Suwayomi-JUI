/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.updates

import ca.gosyer.jui.core.lang.launchDefault
import ca.gosyer.jui.domain.chapter.interactor.DeleteChapterDownload
import ca.gosyer.jui.domain.chapter.interactor.UpdateChapter
import ca.gosyer.jui.domain.chapter.model.Chapter
import ca.gosyer.jui.domain.download.interactor.BatchChapterDownload
import ca.gosyer.jui.domain.download.interactor.QueueChapterDownload
import ca.gosyer.jui.domain.download.interactor.StopChapterDownload
import ca.gosyer.jui.domain.download.service.DownloadService
import ca.gosyer.jui.domain.updates.interactor.GetRecentUpdates
import ca.gosyer.jui.domain.updates.interactor.UpdateLibrary
import ca.gosyer.jui.domain.updates.interactor.UpdatesPager
import ca.gosyer.jui.ui.base.chapter.ChapterDownloadItem
import ca.gosyer.jui.uicore.vm.ContextWrapper
import ca.gosyer.jui.uicore.vm.ViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Inject
import org.lighthousegames.logging.logging

class UpdatesScreenViewModel
    @Inject
    constructor(
        private val queueChapterDownload: QueueChapterDownload,
        private val stopChapterDownload: StopChapterDownload,
        private val deleteChapterDownload: DeleteChapterDownload,
        private val getRecentUpdates: GetRecentUpdates,
        private val updateChapter: UpdateChapter,
        private val batchChapterDownload: BatchChapterDownload,
        private val updateLibrary: UpdateLibrary,
        private val updatesPager: UpdatesPager,
        contextWrapper: ContextWrapper,
    ) : ViewModel(contextWrapper) {
        private val _isLoading = MutableStateFlow(true)
        val isLoading = _isLoading.asStateFlow()

        val updates = updatesPager.updates.map { updates ->
            updates.map {
                when (it) {
                    is UpdatesPager.Updates.Date -> UpdatesUI.Header(it.date)
                    is UpdatesPager.Updates.Update -> UpdatesUI.Item(ChapterDownloadItem(it.manga, it.chapter))
                }
            }.toImmutableList()
        }.stateIn(scope, SharingStarted.Eagerly, persistentListOf())

        private val _selectedIds = MutableStateFlow<ImmutableList<Long>>(persistentListOf())
        val selectedItems = combine(updates, _selectedIds) { updates, selectedItems ->
            updates.asSequence()
                .filterIsInstance<UpdatesUI.Item>()
                .filter { it.chapterDownloadItem.isSelected(selectedItems) }
                .map { it.chapterDownloadItem }
                .toImmutableList()
        }.stateIn(scope, SharingStarted.Eagerly, persistentListOf())

        val inActionMode = _selectedIds.map { it.isNotEmpty() }
            .stateIn(scope, SharingStarted.Eagerly, false)

        init {
            updatesPager.loadNextPage(
                onComplete = {
                    _isLoading.value = false
                },
                onError = {
                    toast(it.message.orEmpty())
                },
            )
            updates
                .map { updates ->
                    updates.filterIsInstance<UpdatesUI.Item>().mapNotNull {
                        it.chapterDownloadItem.manga?.id
                    }.toSet()
                }
                .combine(DownloadService.downloadQueue) { mangaIds, queue ->
                    mangaIds to queue
                }
                .buffer(capacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
                .onEach { (mangaIds, queue) ->
                    val chapters = queue.filter { it.mangaId in mangaIds }
                    updates.value.filterIsInstance<UpdatesUI.Item>().forEach {
                        it.chapterDownloadItem.updateFrom(chapters)
                    }
                }
                .flowOn(Dispatchers.Default)
                .launchIn(scope)
        }

        fun loadNextPage() {
            updatesPager.loadNextPage(
                onError = {
                    toast(it.message.orEmpty())
                },
            )
        }

        private fun setRead(
            chapterIds: List<Long>,
            read: Boolean,
        ) {
            scope.launch {
                updateChapter.await(chapterIds, read = read, onError = { toast(it.message.orEmpty()) })
                _selectedIds.value = persistentListOf()
            }
        }

        fun markRead(id: Long?) = setRead(listOfNotNull(id).ifEmpty { _selectedIds.value }, true)

        fun markUnread(id: Long?) = setRead(listOfNotNull(id).ifEmpty { _selectedIds.value }, false)

        private fun setBookmarked(
            chapterIds: List<Long>,
            bookmark: Boolean,
        ) {
            scope.launch {
                updateChapter.await(chapterIds, bookmarked = bookmark, onError = { toast(it.message.orEmpty()) })
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
            scope.launchDefault {
                if (chapter == null) {
                    val selectedIds = _selectedIds.value
                    deleteChapterDownload.await(selectedIds, onError = { toast(it.message.orEmpty()) })
                    selectedItems.value.forEach {
                        it.setNotDownloaded()
                    }
                    _selectedIds.value = persistentListOf()
                    return@launchDefault
                }
                updates.value
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
            scope.launchDefault {
                updates.value
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
            scope.launchDefault {
                _selectedIds.value = updates.value.filterIsInstance<UpdatesUI.Item>()
                    .map { it.chapterDownloadItem.chapter.id }
                    .toImmutableList()
            }
        }

        fun invertSelection() {
            scope.launchDefault {
                _selectedIds.value = updates.value.filterIsInstance<UpdatesUI.Item>()
                    .map { it.chapterDownloadItem.chapter.id }
                    .minus(_selectedIds.value)
                    .toImmutableList()
            }
        }

        fun selectChapter(id: Long) {
            scope.launchDefault {
                _selectedIds.value = _selectedIds.value.plus(id).toImmutableList()
            }
        }

        fun unselectChapter(id: Long) {
            scope.launchDefault {
                _selectedIds.value = _selectedIds.value.minus(id).toImmutableList()
            }
        }

        fun clearSelection() {
            scope.launchDefault {
                _selectedIds.value = persistentListOf()
            }
        }

        fun updateLibrary() {
            scope.launchDefault { updateLibrary.await(onError = { toast(it.message.orEmpty()) }) }
        }

        override fun onDispose() {
            super.onDispose()
            updatesPager.cancel()
        }

        private companion object {
            private val log = logging()
        }
    }

sealed class UpdatesUI {
    data class Item(
        val chapterDownloadItem: ChapterDownloadItem,
    ) : UpdatesUI()

    data class Header(
        val date: String,
    ) : UpdatesUI()
}
