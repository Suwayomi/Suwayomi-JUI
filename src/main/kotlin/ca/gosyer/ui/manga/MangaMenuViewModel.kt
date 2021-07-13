/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.manga

import ca.gosyer.data.download.DownloadService
import ca.gosyer.data.download.model.DownloadChapter
import ca.gosyer.data.models.Chapter
import ca.gosyer.data.models.Manga
import ca.gosyer.data.server.ServerPreferences
import ca.gosyer.data.server.interactions.ChapterInteractionHandler
import ca.gosyer.data.server.interactions.LibraryInteractionHandler
import ca.gosyer.data.server.interactions.MangaInteractionHandler
import ca.gosyer.data.ui.UiPreferences
import ca.gosyer.ui.base.vm.ViewModel
import ca.gosyer.util.lang.throwIfCancellation
import ca.gosyer.util.lang.withIOContext
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale
import javax.inject.Inject

class MangaMenuViewModel @Inject constructor(
    private val params: Params,
    private val mangaHandler: MangaInteractionHandler,
    private val chapterHandler: ChapterInteractionHandler,
    private val libraryHandler: LibraryInteractionHandler,
    private val downloadService: DownloadService,
    serverPreferences: ServerPreferences,
    uiPreferences: UiPreferences,
) : ViewModel() {
    val serverUrl = serverPreferences.serverUrl().stateIn(scope)

    private val downloadingChapters = downloadService.registerWatch(params.mangaId)

    private val _manga = MutableStateFlow<Manga?>(null)
    val manga = _manga.asStateFlow()

    private val _chapters = MutableStateFlow(emptyList<ViewChapter>())
    val chapters = _chapters.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    val dateTimeFormatter = uiPreferences.dateFormat().changes()
        .map {
            getDateFormat(it)
        }
        .asStateFlow(getDateFormat(uiPreferences.dateFormat().get()))

    init {
        downloadingChapters.mapLatest { downloadingChapters ->
            chapters.value.forEach { chapter ->
                val downloadingChapter = downloadingChapters.find {
                    it.chapterIndex == chapter.chapter.index
                }
                if (downloadingChapter != null && chapter.downloadState.value != DownloadState.Downloading) {
                    chapter.downloadState.value = DownloadState.Downloading
                }
                if (chapter.downloadState.value == DownloadState.Downloading && downloadingChapter == null) {
                    chapter.downloadState.value = DownloadState.Downloaded
                }
                chapter.downloadChapterFlow.value = downloadingChapter
            }
        }.launchIn(scope)

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

    private suspend fun refreshMangaAsync(mangaId: Long, refresh: Boolean = false) = withIOContext {
        async {
            try {
                _manga.value = mangaHandler.getManga(mangaId, refresh)
            } catch (e: Exception) {
                e.throwIfCancellation()
            }
        }
    }

    private suspend fun refreshChaptersAsync(mangaId: Long, refresh: Boolean = false) = withIOContext {
        async {
            try {
                _chapters.value = chapterHandler.getChapters(mangaId, refresh).toViewChapters()
            } catch (e: Exception) {
                e.throwIfCancellation()
            }
        }
    }

    fun toggleFavorite() {
        scope.launch {
            manga.value?.let {
                if (it.inLibrary) {
                    libraryHandler.removeMangaFromLibrary(it)
                } else {
                    libraryHandler.addMangaToLibrary(it)
                }

                refreshMangaAsync(it.id).await()
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
                _chapters.value = chapterHandler.getChapters(manga).toViewChapters()
            }
        }
    }

    fun toggleBookmarked(index: Int) {
        scope.launch {
            manga.value?.let { manga ->
                chapterHandler.updateChapter(manga, index, bookmarked = !_chapters.value.first { it.chapter.index == index }.chapter.bookmarked)
                _chapters.value = chapterHandler.getChapters(manga).toViewChapters()
            }
        }
    }

    fun markPreviousRead(index: Int) {
        scope.launch {
            manga.value?.let { manga ->
                chapterHandler.updateChapter(manga, index, markPreviousRead = true)
                _chapters.value = chapterHandler.getChapters(manga).toViewChapters()
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
            manga.value?.let { manga ->
                chapterHandler.deleteChapterDownload(manga, index)
                chapters.value.find { it.chapter.index == index }?.downloadState?.value = DownloadState.NotDownloaded
            }
        }
    }

    override fun onDestroy() {
        downloadService.removeWatch(params.mangaId)
    }

    private fun List<Chapter>.toViewChapters() = map {
        ViewChapter(
            it,
            MutableStateFlow(
                if (it.downloaded) {
                    DownloadState.Downloaded
                } else {
                    DownloadState.NotDownloaded
                }
            )
        )
    }

    data class ViewChapter(
        val chapter: Chapter,
        val downloadState: MutableStateFlow<DownloadState>,
        val downloadChapterFlow: MutableStateFlow<DownloadChapter?> = MutableStateFlow(null)
    )
    enum class DownloadState {
        NotDownloaded,
        Downloading,
        Downloaded
    }

    data class Params(val mangaId: Long)
}
