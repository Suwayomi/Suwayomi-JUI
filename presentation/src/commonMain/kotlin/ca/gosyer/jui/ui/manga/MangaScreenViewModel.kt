/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.manga

import ca.gosyer.jui.core.lang.withIOContext
import ca.gosyer.jui.data.base.DateHandler
import ca.gosyer.jui.domain.category.interactor.AddMangaToCategory
import ca.gosyer.jui.domain.category.interactor.GetCategories
import ca.gosyer.jui.domain.category.interactor.GetMangaCategories
import ca.gosyer.jui.domain.category.interactor.RemoveMangaFromCategory
import ca.gosyer.jui.domain.category.model.Category
import ca.gosyer.jui.domain.chapter.interactor.BatchUpdateChapter
import ca.gosyer.jui.domain.chapter.interactor.DeleteChapterDownload
import ca.gosyer.jui.domain.chapter.interactor.GetChapters
import ca.gosyer.jui.domain.chapter.interactor.RefreshChapters
import ca.gosyer.jui.domain.chapter.interactor.UpdateChapterMarkPreviousRead
import ca.gosyer.jui.domain.chapter.model.Chapter
import ca.gosyer.jui.domain.download.interactor.BatchChapterDownload
import ca.gosyer.jui.domain.download.interactor.QueueChapterDownload
import ca.gosyer.jui.domain.download.interactor.StopChapterDownload
import ca.gosyer.jui.domain.download.service.DownloadService
import ca.gosyer.jui.domain.library.interactor.AddMangaToLibrary
import ca.gosyer.jui.domain.library.interactor.RemoveMangaFromLibrary
import ca.gosyer.jui.domain.manga.interactor.GetManga
import ca.gosyer.jui.domain.manga.interactor.RefreshManga
import ca.gosyer.jui.domain.manga.model.Manga
import ca.gosyer.jui.domain.ui.service.UiPreferences
import ca.gosyer.jui.ui.base.chapter.ChapterDownloadItem
import ca.gosyer.jui.ui.base.chapter.ChapterDownloadState
import ca.gosyer.jui.ui.base.model.StableHolder
import ca.gosyer.jui.uicore.vm.ContextWrapper
import ca.gosyer.jui.uicore.vm.ViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Inject
import org.lighthousegames.logging.logging

class MangaScreenViewModel @Inject constructor(
    private val dateHandler: DateHandler,
    private val getManga: GetManga,
    private val refreshManga: RefreshManga,
    private val getChapters: GetChapters,
    private val refreshChapters: RefreshChapters,
    private val batchUpdateChapter: BatchUpdateChapter,
    private val updateChapterMarkPreviousRead: UpdateChapterMarkPreviousRead,
    private val queueChapterDownload: QueueChapterDownload,
    private val stopChapterDownload: StopChapterDownload,
    private val deleteChapterDownload: DeleteChapterDownload,
    private val getCategories: GetCategories,
    private val getMangaCategories: GetMangaCategories,
    private val addMangaToCategory: AddMangaToCategory,
    private val removeMangaFromCategory: RemoveMangaFromCategory,
    private val addMangaToLibrary: AddMangaToLibrary,
    private val removeMangaFromLibrary: RemoveMangaFromLibrary,
    private val batchChapterDownload: BatchChapterDownload,
    uiPreferences: UiPreferences,
    contextWrapper: ContextWrapper,
    private val params: Params
) : ViewModel(contextWrapper) {
    private val _manga = MutableStateFlow<Manga?>(null)
    val manga = _manga.asStateFlow()

    private val _chapters = MutableStateFlow<ImmutableList<ChapterDownloadItem>>(persistentListOf())
    val chapters = _chapters.asStateFlow()

    private val _selectedIds = MutableStateFlow<ImmutableList<Long>>(persistentListOf())
    val selectedItems = combine(chapters, _selectedIds) { chapters, selecteditems ->
        chapters.filter { it.isSelected(selecteditems) }.toImmutableList()
    }.stateIn(scope, SharingStarted.Eagerly, persistentListOf())

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    val categories = getCategories.asFlow(true)
        .map { it.toImmutableList() }
        .catch {
            toast(it.message.orEmpty())
            log.warn(it) { "Failed to get categories" }
        }
        .stateIn(scope, SharingStarted.Eagerly, persistentListOf())

    private val _mangaCategories = MutableStateFlow<ImmutableList<Category>>(persistentListOf())
    val mangaCategories = _mangaCategories.asStateFlow()

    val categoriesExist = categories.map { it.isNotEmpty() }
        .stateIn(scope, SharingStarted.Eagerly, true)

    val inActionMode = _selectedIds.map { it.isNotEmpty() }
        .stateIn(scope, SharingStarted.Eagerly, false)

    private val chooseCategoriesFlow = MutableSharedFlow<Unit>()
    val chooseCategoriesFlowHolder = StableHolder(chooseCategoriesFlow.asSharedFlow())

    val dateTimeFormatter = uiPreferences.dateFormat().changes()
        .map {
            dateHandler.getDateFormat(it)
        }
        .asStateFlow(dateHandler.getDateFormat(uiPreferences.dateFormat().get()))

    init {
        DownloadService.registerWatch(params.mangaId)
            .mapLatest { downloadingChapters ->
                chapters.value.forEach { chapter ->
                    chapter.updateFrom(downloadingChapters)
                }
            }
            .launchIn(scope)

        scope.launch {
            refreshMangaAsync(params.mangaId).await() to refreshChaptersAsync(params.mangaId).await()
            _isLoading.value = false
        }
    }

    fun loadManga() {
        scope.launch {
            _isLoading.value = true
            refreshMangaAsync(params.mangaId).await() to refreshChaptersAsync(params.mangaId).await()
            _isLoading.value = false
        }
    }

    fun loadChapters() {
        scope.launch {
            _isLoading.value = true
            refreshChaptersAsync(params.mangaId).await()
            _isLoading.value = false
        }
    }

    fun refreshManga() {
        scope.launch {
            _isLoading.value = true
            refreshMangaAsync(params.mangaId, true).await() to refreshChaptersAsync(params.mangaId, true).await()
            _isLoading.value = false
        }
    }

    fun setCategories() {
        scope.launch {
            manga.value ?: return@launch
            chooseCategoriesFlow.emit(Unit)
        }
    }

    private suspend fun refreshMangaAsync(mangaId: Long, refresh: Boolean = false) = withIOContext {
        async {
            val manga = if (refresh) {
                refreshManga.await(mangaId, onError = { toast(it.message.orEmpty()) })
            } else {
                getManga.await(mangaId, onError = { toast(it.message.orEmpty()) })
            }
            if (manga != null) {
                _manga.value = manga
            }

            val mangaCategories = getMangaCategories.await(mangaId, onError = { toast(it.message.orEmpty()) })
            if (mangaCategories != null) {
                _mangaCategories.value = mangaCategories.toImmutableList()
            }
        }
    }

    private suspend fun refreshChaptersAsync(mangaId: Long, refresh: Boolean = false) = withIOContext {
        async {
            val chapters = if (refresh) {
                refreshChapters.await(mangaId, onError = { toast(it.message.orEmpty()) })
            } else {
                getChapters.await(mangaId, onError = { toast(it.message.orEmpty()) })
            }
            if (chapters != null) {
                _chapters.value = chapters.toDownloadChapters()
            }
        }
    }

    fun toggleFavorite() {
        scope.launch {
            manga.value?.let { manga ->
                if (manga.inLibrary) {
                    removeMangaFromLibrary.await(manga, onError = { toast(it.message.orEmpty()) })
                    refreshMangaAsync(manga.id).await()
                } else {
                    if (categories.value.isEmpty()) {
                        addFavorite(emptyList(), emptyList())
                    } else {
                        chooseCategoriesFlow.emit(Unit)
                    }
                }
            }
        }
    }

    fun addFavorite(categories: List<Category>, oldCategories: List<Category>) {
        scope.launch {
            manga.value?.let { manga ->
                if (manga.inLibrary) {
                    oldCategories.filterNot { it in categories }.forEach {
                        removeMangaFromCategory.await(manga, it, onError = { toast(it.message.orEmpty()) })
                    }
                } else {
                    addMangaToLibrary.await(manga, onError = { toast(it.message.orEmpty()) })
                }
                categories.filterNot { it in oldCategories }.forEach {
                    addMangaToCategory.await(manga, it, onError = { toast(it.message.orEmpty()) })
                }
                refreshMangaAsync(manga.id).await()
            }
        }
    }

    private fun setRead(chapterIds: List<Long>, read: Boolean) {
        scope.launch {
            manga.value?.let { manga ->
                batchUpdateChapter.await(manga, chapterIds, isRead = read, onError = { toast(it.message.orEmpty()) })
                refreshChaptersAsync(manga.id).await()
                _selectedIds.value = persistentListOf()
            }
        }
    }
    fun markRead(id: Long?) = setRead(listOfNotNull(id).ifEmpty { _selectedIds.value }, true)
    fun markUnread(id: Long?) = setRead(listOfNotNull(id).ifEmpty { _selectedIds.value }, false)

    private fun setBookmarked(chapterIds: List<Long>, bookmark: Boolean) {
        scope.launch {
            manga.value?.let { manga ->
                batchUpdateChapter.await(manga, chapterIds, isBookmarked = bookmark, onError = { toast(it.message.orEmpty()) })
                refreshChaptersAsync(manga.id).await()
                _selectedIds.value = persistentListOf()
            }
        }
    }
    fun bookmarkChapter(id: Long?) = setBookmarked(listOfNotNull(id).ifEmpty { _selectedIds.value }, true)
    fun unBookmarkChapter(id: Long?) = setBookmarked(listOfNotNull(id).ifEmpty { _selectedIds.value }, false)

    fun markPreviousRead(index: Int) {
        scope.launch {
            manga.value?.let { manga ->
                updateChapterMarkPreviousRead.await(manga, index, onError = { toast(it.message.orEmpty()) })
                refreshChaptersAsync(manga.id).await()
                _selectedIds.value = persistentListOf()
            }
        }
    }

    fun downloadChapter(index: Int) {
        manga.value?.let { manga ->
            scope.launch { queueChapterDownload.await(manga, index, onError = { toast(it.message.orEmpty()) }) }
        }
    }

    fun deleteDownload(id: Long?) {
        scope.launch {
            if (id == null) {
                val manga = _manga.value ?: return@launch
                val chapterIds = _selectedIds.value
                batchUpdateChapter.await(manga, chapterIds, delete = true, onError = { toast(it.message.orEmpty()) })
                chapterIds.forEach { id ->
                    chapters.value.find { it.chapter.id == id }?.setNotDownloaded()
                }
                _selectedIds.value = persistentListOf()
            } else {
                chapters.value.find { it.chapter.id == id }
                    ?.deleteDownload(deleteChapterDownload)
            }
        }
    }

    fun stopDownloadingChapter(index: Int) {
        scope.launch {
            chapters.value.find { it.chapter.index == index }
                ?.stopDownloading(stopChapterDownload)
        }
    }

    fun selectAll() {
        scope.launch {
            _selectedIds.value = chapters.value.map { it.chapter.id }.toImmutableList()
        }
    }

    fun invertSelection() {
        scope.launch {
            _selectedIds.value = chapters.value.map { it.chapter.id }.minus(_selectedIds.value).toImmutableList()
        }
    }

    fun selectChapter(index: Int) {
        scope.launch {
            chapters.value.find { it.chapter.index == index }
                ?.let { _selectedIds.value = _selectedIds.value.plus(it.chapter.id).toImmutableList() }
        }
    }
    fun unselectChapter(index: Int) {
        scope.launch {
            chapters.value.find { it.chapter.index == index }
                ?.let { _selectedIds.value = _selectedIds.value.minus(it.chapter.id).toImmutableList() }
        }
    }

    fun clearSelection() {
        scope.launch {
            _selectedIds.value = persistentListOf()
        }
    }

    fun downloadChapters() {
        scope.launch {
            batchChapterDownload.await(_selectedIds.value)
            _selectedIds.value = persistentListOf()
        }
    }

    fun downloadNext(next: Int) {
        scope.launch {
            batchChapterDownload.await(
                _chapters.value.filter { !it.chapter.read && it.downloadState.value == ChapterDownloadState.NotDownloaded }
                    .map { it.chapter.id }
                    .takeLast(next)
            )
        }
    }

    fun downloadUnread() {
        scope.launch {
            batchChapterDownload.await(
                _chapters.value.filter { !it.chapter.read && it.downloadState.value == ChapterDownloadState.NotDownloaded }
                    .map { it.chapter.id }
            )
        }
    }

    fun downloadAll() {
        scope.launch {
            batchChapterDownload.await(
                _chapters.value
                    .filter { it.downloadState.value == ChapterDownloadState.NotDownloaded }
                    .map { it.chapter.id }
            )
        }
    }

    private fun List<Chapter>.toDownloadChapters() = map {
        ChapterDownloadItem(null, it)
    }.toImmutableList()

    data class Params(val mangaId: Long)

    private companion object {
        private val log = logging()
    }
}
