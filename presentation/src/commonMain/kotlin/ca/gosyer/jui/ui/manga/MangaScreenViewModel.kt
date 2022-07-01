/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.manga

import ca.gosyer.jui.core.lang.withIOContext
import ca.gosyer.jui.data.base.DateHandler
import ca.gosyer.jui.data.chapter.ChapterRepositoryImpl
import ca.gosyer.jui.domain.category.interactor.AddMangaToCategory
import ca.gosyer.jui.domain.category.interactor.GetCategories
import ca.gosyer.jui.domain.category.interactor.GetMangaCategories
import ca.gosyer.jui.domain.category.interactor.RemoveMangaFromCategory
import ca.gosyer.jui.domain.category.model.Category
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
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Inject
import org.lighthousegames.logging.logging

class MangaScreenViewModel @Inject constructor(
    private val dateHandler: DateHandler,
    private val getManga: GetManga,
    private val refreshManga: RefreshManga,
    private val chapterHandler: ChapterRepositoryImpl,
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
            }
            getMangaCategories.await(mangaId)
                ?.let {
                    _mangaCategories.value = it
                }
        }
    }

    private suspend fun refreshChaptersAsync(mangaId: Long, refresh: Boolean = false) = withIOContext {
        async {
            _chapters.value = chapterHandler.getChapters(mangaId, refresh)
                .catch {
                    log.warn(it) { "Error getting chapters" }
                    emit(emptyList())
                }
                .single()
                .toDownloadChapters()
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

    fun toggleRead(index: Int) {
        scope.launch {
            manga.value?.let { manga ->
                chapterHandler.updateChapter(
                    manga.id,
                    index,
                    read = !_chapters.value.first { it.chapter.index == index }.chapter.read
                )
                    .catch {
                        log.warn(it) { "Error toggling read" }
                    }
                    .collect()
                _chapters.value = chapterHandler.getChapters(manga.id)
                    .catch {
                        log.warn(it) { "Error getting new chapters after toggling read" }
                        emit(emptyList())
                    }
                    .single()
                    .toDownloadChapters()
            }
        }
    }

    fun toggleBookmarked(index: Int) {
        scope.launch {
            manga.value?.let { manga ->
                chapterHandler.updateChapter(
                    manga.id,
                    index,
                    bookmarked = !_chapters.value.first { it.chapter.index == index }.chapter.bookmarked
                )
                    .catch {
                        log.warn(it) { "Error toggling bookmarked" }
                    }
                    .collect()
                _chapters.value = chapterHandler.getChapters(manga.id)
                    .catch {
                        log.warn(it) { "Error getting new chapters after toggling bookmarked" }
                        emit(emptyList())
                    }
                    .single()
                    .toDownloadChapters()
            }
        }
    }

    fun markPreviousRead(index: Int) {
        scope.launch {
            manga.value?.let { manga ->
                chapterHandler.updateChapter(manga.id, index, markPreviousRead = true)
                    .catch {
                        log.warn(it) { "Error marking previous as read" }
                    }
                    .collect()
                _chapters.value = chapterHandler.getChapters(manga.id)
                    .catch {
                        log.warn(it) { "Error getting new chapters after marking previous as read" }
                        emit(emptyList())
                    }
                    .single()
                    .toDownloadChapters()
            }
        }
    }

    fun downloadChapter(index: Int) {
        manga.value?.let { manga ->
            chapterHandler.queueChapterDownload(manga.id, index)
                .catch {
                    log.warn(it) { "Error downloading chapter" }
                }
                .launchIn(scope)
        }
    }

    fun deleteDownload(index: Int) {
        chapters.value.find { it.chapter.index == index }
            ?.deleteDownload(chapterHandler)
            ?.catch {
                log.warn(it) { "Error deleting download" }
            }
            ?.launchIn(scope)
    }

    fun stopDownloadingChapter(index: Int) {
        chapters.value.find { it.chapter.index == index }
            ?.stopDownloading(chapterHandler)
            ?.catch {
                log.warn(it) { "Error stopping download" }
            }
            ?.launchIn(scope)
    }

    private fun List<Chapter>.toDownloadChapters() = map {
        ChapterDownloadItem(null, it)
    }

    data class Params(val mangaId: Long)

    private companion object {
        private val log = logging()
    }
}
