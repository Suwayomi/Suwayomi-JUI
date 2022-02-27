/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.manga

import ca.gosyer.core.lang.throwIfCancellation
import ca.gosyer.core.lang.withIOContext
import ca.gosyer.data.download.DownloadService
import ca.gosyer.data.models.Category
import ca.gosyer.data.models.Chapter
import ca.gosyer.data.models.Manga
import ca.gosyer.data.server.interactions.CategoryInteractionHandler
import ca.gosyer.data.server.interactions.ChapterInteractionHandler
import ca.gosyer.data.server.interactions.LibraryInteractionHandler
import ca.gosyer.data.server.interactions.MangaInteractionHandler
import ca.gosyer.data.ui.UiPreferences
import ca.gosyer.ui.base.chapter.ChapterDownloadItem
import ca.gosyer.uicore.vm.ContextWrapper
import ca.gosyer.uicore.vm.ViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Inject
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

class MangaScreenViewModel @Inject constructor(
    private val mangaHandler: MangaInteractionHandler,
    private val chapterHandler: ChapterInteractionHandler,
    private val categoryHandler: CategoryInteractionHandler,
    private val libraryHandler: LibraryInteractionHandler,
    private val downloadService: DownloadService,
    uiPreferences: UiPreferences,
    contextWrapper: ContextWrapper,
    private val params: Params,
) : ViewModel(contextWrapper) {
    private val downloadingChapters = downloadService.registerWatch(params.mangaId)

    private val _manga = MutableStateFlow<Manga?>(null)
    val manga = _manga.asStateFlow()

    private val _chapters = MutableStateFlow(emptyList<ChapterDownloadItem>())
    val chapters = _chapters.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val _categories = MutableStateFlow(emptyList<Category>())
    val categories = _categories.asStateFlow()

    private val _mangaCategories = MutableStateFlow(emptyList<Category>())
    val mangaCategories = _mangaCategories.asStateFlow()

    val categoriesExist = categories.map { it.isNotEmpty() }
        .stateIn(scope, SharingStarted.Eagerly, true)

    val chooseCategoriesFlow = MutableSharedFlow<Unit>()

    val dateTimeFormatter = uiPreferences.dateFormat().changes()
        .map {
            getDateFormat(it)
        }
        .asStateFlow(getDateFormat(uiPreferences.dateFormat().get()))

    init {
        downloadingChapters.mapLatest { (_, downloadingChapters) ->
            chapters.value.forEach { chapter ->
                chapter.updateFrom(downloadingChapters)
            }
        }.launchIn(scope)

        scope.launch {
            refreshMangaAsync(params.mangaId).await() to refreshChaptersAsync(params.mangaId).await()
            _isLoading.value = false
        }

        scope.launch {
            _categories.value = categoryHandler.getCategories(true)
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
            try {
                _manga.value = mangaHandler.getManga(mangaId, refresh)
                _mangaCategories.value = categoryHandler.getMangaCategories(mangaId)
            } catch (e: Exception) {
                e.throwIfCancellation()
            }
        }
    }

    private suspend fun refreshChaptersAsync(mangaId: Long, refresh: Boolean = false) = withIOContext {
        async {
            try {
                _chapters.value = chapterHandler.getChapters(mangaId, refresh).toDownloadChapters()
            } catch (e: Exception) {
                e.throwIfCancellation()
            }
        }
    }

    fun toggleFavorite() {
        scope.launch {
            manga.value?.let { manga ->
                if (manga.inLibrary) {
                    libraryHandler.removeMangaFromLibrary(manga)
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
                        categoryHandler.removeMangaFromCategory(manga, it)
                    }
                } else {
                    libraryHandler.addMangaToLibrary(manga)
                }
                categories.filterNot { it in oldCategories }.forEach {
                    categoryHandler.addMangaToCategory(manga, it)
                }
                refreshMangaAsync(manga.id).await()
            }
        }
    }

    private fun getDateFormat(format: String): DateTimeFormatter = when (format) {
        "" -> DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)
            .withLocale(Locale.getDefault())
            .withZone(ZoneId.systemDefault())
        else -> DateTimeFormatter.ofPattern(format)
            .withZone(ZoneId.systemDefault())
    }

    fun toggleRead(index: Int) {
        scope.launch {
            manga.value?.let { manga ->
                chapterHandler.updateChapter(manga, index, read = !_chapters.value.first { it.chapter.index == index }.chapter.read)
                _chapters.value = chapterHandler.getChapters(manga).toDownloadChapters()
            }
        }
    }

    fun toggleBookmarked(index: Int) {
        scope.launch {
            manga.value?.let { manga ->
                chapterHandler.updateChapter(manga, index, bookmarked = !_chapters.value.first { it.chapter.index == index }.chapter.bookmarked)
                _chapters.value = chapterHandler.getChapters(manga).toDownloadChapters()
            }
        }
    }

    fun markPreviousRead(index: Int) {
        scope.launch {
            manga.value?.let { manga ->
                chapterHandler.updateChapter(manga, index, markPreviousRead = true)
                _chapters.value = chapterHandler.getChapters(manga).toDownloadChapters()
            }
        }
    }

    fun downloadChapter(index: Int) {
        scope.launch {
            manga.value?.let { manga ->
                chapterHandler.queueChapterDownload(manga, index)
            }
        }
    }

    fun deleteDownload(index: Int) {
        scope.launch {
            chapters.value.find { it.chapter.index == index }?.deleteDownload(chapterHandler)
        }
    }

    fun stopDownloadingChapter(index: Int) {
        scope.launch {
            chapters.value.find { it.chapter.index == index }?.stopDownloading(chapterHandler)
        }
    }

    override fun onDispose() {
        downloadService.removeWatch(params.mangaId)
    }

    private fun List<Chapter>.toDownloadChapters() = map {
        ChapterDownloadItem(null, it)
    }

    data class Params(val mangaId: Long)
}
