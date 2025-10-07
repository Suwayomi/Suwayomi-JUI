/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.manga

import ca.gosyer.jui.core.util.DateHandler
import ca.gosyer.jui.domain.category.interactor.AddMangaToCategory
import ca.gosyer.jui.domain.category.interactor.GetCategories
import ca.gosyer.jui.domain.category.interactor.GetMangaCategories
import ca.gosyer.jui.domain.category.interactor.RemoveMangaFromCategory
import ca.gosyer.jui.domain.category.model.Category
import ca.gosyer.jui.domain.chapter.interactor.DeleteChapterDownload
import ca.gosyer.jui.domain.chapter.interactor.GetChapters
import ca.gosyer.jui.domain.chapter.interactor.RefreshChapters
import ca.gosyer.jui.domain.chapter.interactor.UpdateChapter
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
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject
import com.diamondedge.logging.logging

@Inject
class MangaScreenViewModel(
    private val dateHandler: DateHandler,
    private val getManga: GetManga,
    private val refreshManga: RefreshManga,
    private val getChapters: GetChapters,
    private val refreshChapters: RefreshChapters,
    private val updateChapter: UpdateChapter,
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
    @Assisted private val params: Params,
) : ViewModel(contextWrapper) {
    private val _manga = MutableStateFlow<Manga?>(null)
    val manga = _manga.asStateFlow()

    private val _chapters = MutableStateFlow<ImmutableList<ChapterDownloadItem>>(persistentListOf())
    val chapters = _chapters.asStateFlow()

    private val selectedIds = MutableStateFlow<ImmutableList<Long>>(persistentListOf())
    val selectedItems = combine(chapters, selectedIds) { chapters, selecteditems ->
        chapters.filter { it.isSelected(selecteditems) }.toImmutableList()
    }.stateIn(scope, SharingStarted.Eagerly, persistentListOf())

    private val loadingManga = MutableStateFlow(true)
    private val loadingChapters = MutableStateFlow(true)
    private val refreshingChapters = MutableStateFlow(false)
    private val refreshingManga = MutableStateFlow(false)
    val isLoading =
        combine(loadingManga, loadingChapters, refreshingManga, refreshingChapters) { a, b, c, d -> a || b || c || d }
            .stateIn(scope, SharingStarted.Eagerly, true)

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

    val inActionMode = selectedIds.map { it.isNotEmpty() }
        .stateIn(scope, SharingStarted.Eagerly, false)

    private val chooseCategoriesFlow = MutableSharedFlow<Unit>()
    val chooseCategoriesFlowHolder = StableHolder(chooseCategoriesFlow.asSharedFlow())

    private val reloadManga = MutableSharedFlow<Unit>()
    private val reloadChapters = MutableSharedFlow<Unit>()

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

        reloadManga.onStart { emit(Unit) }.flatMapLatest {
            loadingManga.value = true
            getManga.asFlow(params.mangaId)
        }
            .onEach {
                _manga.value = it
                loadingManga.value = false
            }
            .catch {
                toast(it.message.orEmpty())
                log.warn(it) { "Error when loading manga" }
                loadingManga.value = false
            }
            .launchIn(scope)

        reloadChapters.onStart { emit(Unit) }.flatMapLatest {
            loadingChapters.value = true
            getChapters.asFlow(params.mangaId)
        }
            .onEach {
                updateChapters(it)
                loadingChapters.value = false
            }
            .catch {
                toast(it.message.orEmpty())
                log.warn(it) { "Error when getting chapters" }
                loadingChapters.value = false
            }
            .launchIn(scope)

        scope.launch {
            val mangaCategories = getMangaCategories.await(params.mangaId, onError = { toast(it.message.orEmpty()) })
            if (mangaCategories != null) {
                _mangaCategories.value = mangaCategories.toImmutableList()
            }
        }

        scope.launch {
            val manga = manga.first { it != null }!!
            if (!manga.initialized) {
                refreshManga()
            }
        }
    }

    fun loadManga() {
        scope.launch {
            reloadManga.emit(Unit)
        }
    }

    fun loadChapters() {
        scope.launch {
            reloadChapters.emit(Unit)
        }
    }

    fun updateChapters(chapters: List<Chapter>) {
        _chapters.value = chapters.sortedByDescending { it.index }.toDownloadChapters()
    }

    fun refreshManga() {
        scope.launch {
            refreshingManga.value = true
            val manga = refreshManga.await(
                params.mangaId,
                onError = {
                    log.warn(it) { "Error when refreshing manga" }
                    toast(it.message.orEmpty())
                },
            )
            if (manga != null) {
                _manga.value = manga
            }
            refreshingManga.value = false
        }
        scope.launch {
            refreshingChapters.value = true
            val chapters = refreshChapters.await(
                params.mangaId,
                onError = {
                    log.warn(it) { "Error when refreshing chapters" }
                    toast(it.message.orEmpty())
                },
            )
            if (!chapters.isNullOrEmpty()) {
                updateChapters(chapters)
            }
            refreshingChapters.value = false
        }
    }

    fun setCategories() {
        scope.launch {
            manga.value ?: return@launch
            chooseCategoriesFlow.emit(Unit)
        }
    }

    fun toggleFavorite() {
        scope.launch {
            manga.value?.let { manga ->
                if (manga.inLibrary) {
                    removeMangaFromLibrary.await(manga, onError = { toast(it.message.orEmpty()) })
                } else {
                    if (categories.value.isEmpty()) {
                        addFavorite(emptyList(), emptyList())
                    } else {
                        chooseCategoriesFlow.emit(Unit)
                    }
                }
                loadManga()
            }
        }
    }

    fun addFavorite(
        categories: List<Category>,
        oldCategories: List<Category>,
    ) {
        scope.launch {
            manga.value?.let { manga ->
                if (manga.inLibrary) {
                    if (oldCategories.isEmpty()) {
                        removeMangaFromCategory.await(manga.id, 0, onError = { toast(it.message.orEmpty()) })
                    } else {
                        oldCategories.filterNot { it in categories }.forEach {
                            removeMangaFromCategory.await(manga, it, onError = { toast(it.message.orEmpty()) })
                        }
                    }
                } else {
                    addMangaToLibrary.await(manga, onError = { toast(it.message.orEmpty()) })
                }
                if (categories.isEmpty()) {
                    addMangaToCategory.await(manga.id, 0, onError = { toast(it.message.orEmpty()) })
                } else {
                    categories.filterNot { it in oldCategories }.forEach {
                        addMangaToCategory.await(manga, it, onError = { toast(it.message.orEmpty()) })
                    }
                }

                val mangaCategories = getMangaCategories.await(manga.id, onError = { toast(it.message.orEmpty()) })
                if (mangaCategories != null) {
                    _mangaCategories.value = mangaCategories.toImmutableList()
                }

                loadManga()
            }
        }
    }

    private fun setRead(
        chapterIds: List<Long>,
        read: Boolean,
    ) {
        scope.launch {
            manga.value?.let {
                updateChapter.await(chapterIds, listOf(params.mangaId), read = read, onError = { toast(it.message.orEmpty()) })
                selectedIds.value = persistentListOf()
                loadChapters()
            }
        }
    }

    fun markRead(id: Long?) = setRead(listOfNotNull(id).ifEmpty { selectedIds.value }, true)

    fun markUnread(id: Long?) = setRead(listOfNotNull(id).ifEmpty { selectedIds.value }, false)

    private fun setBookmarked(
        chapterIds: List<Long>,
        bookmark: Boolean,
    ) {
        scope.launch {
            manga.value?.let {
                updateChapter.await(chapterIds, listOf(params.mangaId), bookmarked = bookmark, onError = { toast(it.message.orEmpty()) })
                selectedIds.value = persistentListOf()
                loadChapters()
            }
        }
    }

    fun bookmarkChapter(id: Long?) = setBookmarked(listOfNotNull(id).ifEmpty { selectedIds.value }, true)

    fun unBookmarkChapter(id: Long?) = setBookmarked(listOfNotNull(id).ifEmpty { selectedIds.value }, false)

    fun markPreviousRead(index: Int) {
        scope.launch {
            manga.value?.let {
                val chapters = chapters.value
                    .sortedBy { it.chapter.index }
                    .subList(0, index).map { it.chapter.id } // todo test
                updateChapter.await(chapters, listOf(params.mangaId), read = true, onError = { toast(it.message.orEmpty()) })
                selectedIds.value = persistentListOf()
                loadChapters()
            }
        }
    }

    fun downloadChapter(chapterId: Long) {
        scope.launch { queueChapterDownload.await(chapterId, onError = { toast(it.message.orEmpty()) }) }
    }

    fun deleteDownload(id: Long?) {
        scope.launch {
            if (id == null) {
                val chapterIds = selectedIds.value
                deleteChapterDownload.await(chapterIds, listOf(params.mangaId), onError = { toast(it.message.orEmpty()) })
                selectedItems.value.forEach {
                    it.setNotDownloaded()
                }
                selectedIds.value = persistentListOf()
            } else {
                chapters.value.find { it.chapter.id == id }
                    ?.deleteDownload(deleteChapterDownload)
            }
        }
    }

    fun stopDownloadingChapter(chapterId: Long) {
        scope.launch {
            chapters.value.find { it.chapter.id == chapterId }
                ?.stopDownloading(stopChapterDownload)
        }
    }

    fun selectAll() {
        scope.launch {
            selectedIds.value = chapters.value.map { it.chapter.id }.toImmutableList()
        }
    }

    fun invertSelection() {
        scope.launch {
            selectedIds.value = chapters.value.map { it.chapter.id }.minus(selectedIds.value).toImmutableList()
        }
    }

    fun selectChapter(id: Long) {
        scope.launch {
            selectedIds.value = selectedIds.value.plus(id).toImmutableList()
        }
    }

    fun unselectChapter(id: Long) {
        scope.launch {
            selectedIds.value = selectedIds.value.minus(id).toImmutableList()
        }
    }

    fun clearSelection() {
        scope.launch {
            selectedIds.value = persistentListOf()
        }
    }

    fun downloadChapters() {
        scope.launch {
            batchChapterDownload.await(selectedIds.value)
            selectedIds.value = persistentListOf()
        }
    }

    fun downloadNext(next: Int) {
        scope.launch {
            batchChapterDownload.await(
                _chapters.value.filter { !it.chapter.read && it.downloadState.value == ChapterDownloadState.NotDownloaded }
                    .map { it.chapter.id }
                    .takeLast(next),
            )
        }
    }

    fun downloadUnread() {
        scope.launch {
            batchChapterDownload.await(
                _chapters.value.filter { !it.chapter.read && it.downloadState.value == ChapterDownloadState.NotDownloaded }
                    .map { it.chapter.id },
            )
        }
    }

    fun downloadAll() {
        scope.launch {
            batchChapterDownload.await(
                _chapters.value
                    .filter { it.downloadState.value == ChapterDownloadState.NotDownloaded }
                    .map { it.chapter.id },
            )
        }
    }

    private fun List<Chapter>.toDownloadChapters() =
        map {
            ChapterDownloadItem(null, it)
        }.toImmutableList()

    data class Params(
        val mangaId: Long,
    )

    private companion object {
        private val log = logging()
    }
}
