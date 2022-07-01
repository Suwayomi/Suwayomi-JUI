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
import ca.gosyer.jui.domain.chapter.interactor.DeleteChapterDownload
import ca.gosyer.jui.domain.chapter.interactor.GetChapters
import ca.gosyer.jui.domain.chapter.interactor.QueueChapterDownload
import ca.gosyer.jui.domain.chapter.interactor.RefreshChapters
import ca.gosyer.jui.domain.chapter.interactor.StopChapterDownload
import ca.gosyer.jui.domain.chapter.interactor.UpdateChapterFlags
import ca.gosyer.jui.domain.chapter.model.Chapter
import ca.gosyer.jui.domain.download.service.DownloadService
import ca.gosyer.jui.domain.library.interactor.AddMangaToLibrary
import ca.gosyer.jui.domain.library.interactor.RemoveMangaFromLibrary
import ca.gosyer.jui.domain.manga.interactor.GetManga
import ca.gosyer.jui.domain.manga.interactor.RefreshManga
import ca.gosyer.jui.domain.manga.model.Manga
import ca.gosyer.jui.domain.ui.service.UiPreferences
import ca.gosyer.jui.ui.base.chapter.ChapterDownloadItem
import ca.gosyer.jui.uicore.vm.ContextWrapper
import ca.gosyer.jui.uicore.vm.ViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
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
    private val updateChapterFlags: UpdateChapterFlags,
    private val queueChapterDownload: QueueChapterDownload,
    private val stopChapterDownload: StopChapterDownload,
    private val deleteChapterDownload: DeleteChapterDownload,
    private val getCategories: GetCategories,
    private val getMangaCategories: GetMangaCategories,
    private val addMangaToCategory: AddMangaToCategory,
    private val removeMangaFromCategory: RemoveMangaFromCategory,
    private val addMangaToLibrary: AddMangaToLibrary,
    private val removeMangaFromLibrary: RemoveMangaFromLibrary,
    uiPreferences: UiPreferences,
    contextWrapper: ContextWrapper,
    private val params: Params,
) : ViewModel(contextWrapper) {
    private val _manga = MutableStateFlow<Manga?>(null)
    val manga = _manga.asStateFlow()

    private val _chapters = MutableStateFlow(emptyList<ChapterDownloadItem>())
    val chapters = _chapters.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    val categories = getCategories.asFlow(true)
        .catch { log.warn(it) { "Failed to get categories" } }
        .stateIn(scope, SharingStarted.Eagerly, emptyList())

    private val _mangaCategories = MutableStateFlow(emptyList<Category>())
    val mangaCategories = _mangaCategories.asStateFlow()

    val categoriesExist = categories.map { it.isNotEmpty() }
        .stateIn(scope, SharingStarted.Eagerly, true)

    val chooseCategoriesFlow = MutableSharedFlow<Unit>()

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
                refreshManga.await(mangaId)
            } else {
                getManga.await(mangaId)
            }
            if (manga != null) {
                _manga.value = manga
            } else {
                // TODO: 2022-07-01 Error toast
            }

            val mangaCategories = getMangaCategories.await(mangaId)
            if (mangaCategories != null) {
                _mangaCategories.value = mangaCategories
            } else {
                // TODO: 2022-07-01 Error toast
            }
        }
    }

    private suspend fun refreshChaptersAsync(mangaId: Long, refresh: Boolean = false) = withIOContext {
        async {
            val chapters = if (refresh) {
                refreshChapters.await(mangaId)
            } else {
                getChapters.await(mangaId)
            }
            if (chapters != null) {
                _chapters.value = chapters.toDownloadChapters()
            } else {
                // TODO: 2022-07-01 Error toast
            }
        }
    }

    fun toggleFavorite() {
        scope.launch {
            manga.value?.let { manga ->
                if (manga.inLibrary) {
                    removeMangaFromLibrary.await(manga)
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
                        removeMangaFromCategory.await(manga, it)
                    }
                } else {
                    addMangaToLibrary.await(manga)
                }
                categories.filterNot { it in oldCategories }.forEach {
                    addMangaToCategory.await(manga, it)
                }
                refreshMangaAsync(manga.id).await()
            }
        }
    }

    private fun findChapter(index: Int) = chapters.value.find { it.chapter.index == index }?.chapter

    fun toggleRead(index: Int) {
        val chapter = findChapter(index) ?: return
        scope.launch {
            manga.value?.let { manga ->
                updateChapterFlags.await(manga, index, read = chapter.read.not())
                refreshChaptersAsync(manga.id).await()
            }
        }
    }

    fun toggleBookmarked(index: Int) {
        val chapter = findChapter(index) ?: return
        scope.launch {
            manga.value?.let { manga ->
                updateChapterFlags.await(manga, index, bookmarked = chapter.bookmarked.not())
                refreshChaptersAsync(manga.id).await()
            }
        }
    }

    fun markPreviousRead(index: Int) {
        scope.launch {
            manga.value?.let { manga ->
                updateChapterFlags.await(manga, index, markPreviousRead = true)
                refreshChaptersAsync(manga.id).await()
            }
        }
    }

    fun downloadChapter(index: Int) {
        manga.value?.let { manga ->
            scope.launch { queueChapterDownload.await(manga, index) }
        }
    }

    fun deleteDownload(index: Int) {
        scope.launch {
            chapters.value.find { it.chapter.index == index }
                ?.deleteDownload(deleteChapterDownload)
        }
    }

    fun stopDownloadingChapter(index: Int) {
        scope.launch {
            chapters.value.find { it.chapter.index == index }
                ?.stopDownloading(stopChapterDownload)
        }
    }

    private fun List<Chapter>.toDownloadChapters() = map {
        ChapterDownloadItem(null, it)
    }

    data class Params(val mangaId: Long)

    private companion object {
        private val log = logging()
    }
}
