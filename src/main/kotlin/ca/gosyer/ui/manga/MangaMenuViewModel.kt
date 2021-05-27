/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.manga

import ca.gosyer.data.models.Chapter
import ca.gosyer.data.models.Manga
import ca.gosyer.data.server.ServerPreferences
import ca.gosyer.data.server.interactions.ChapterInteractionHandler
import ca.gosyer.data.server.interactions.LibraryInteractionHandler
import ca.gosyer.data.server.interactions.MangaInteractionHandler
import ca.gosyer.data.ui.UiPreferences
import ca.gosyer.ui.base.vm.ViewModel
import ca.gosyer.util.lang.withIOContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale
import javax.inject.Inject

class MangaMenuViewModel @Inject constructor(
    private val params: Params,
    private val uiPreferences: UiPreferences,
    private val mangaHandler: MangaInteractionHandler,
    private val chapterHandler: ChapterInteractionHandler,
    private val libraryHandler: LibraryInteractionHandler,
    serverPreferences: ServerPreferences
) : ViewModel() {
    val serverUrl = serverPreferences.server().stateIn(scope)

    private val _manga = MutableStateFlow<Manga?>(null)
    val manga = _manga.asStateFlow()

    private val _chapters = MutableStateFlow(emptyList<Chapter>())
    val chapters = _chapters.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    val dateTimeFormatter = uiPreferences.dateFormat().changes()
        .map {
            getDateFormat(it)
        }
        .asStateFlow(getDateFormat(uiPreferences.dateFormat().get()))

    init {
        scope.launch {
            refreshMangaAsync(params.mangaId).await() to refreshChaptersAsync(params.mangaId).await()
            _isLoading.value = false
        }
    }

    private suspend fun refreshMangaAsync(mangaId: Long) = withIOContext {
        async {
            try {
                _manga.value = mangaHandler.getManga(mangaId)
            } catch (e: Exception) {
                if (e is CancellationException) throw e
            }
        }
    }

    private suspend fun refreshChaptersAsync(mangaId: Long) = withIOContext {
        async {
            try {
                _chapters.value = chapterHandler.getChapters(mangaId)
            } catch (e: Exception) {
                if (e is CancellationException) throw e
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
                chapterHandler.updateChapter(manga, index, read = !_chapters.value.first { it.index == index }.read)
                _chapters.value = chapterHandler.getChapters(manga)
            }
        }
    }

    fun toggleBookmarked(index: Int) {
        scope.launch {
            manga.value?.let { manga ->
                chapterHandler.updateChapter(manga, index, bookmarked = !_chapters.value.first { it.index == index }.bookmarked)
                _chapters.value = chapterHandler.getChapters(manga)
            }
        }
    }

    fun markPreviousRead(index: Int) {
        scope.launch {
            manga.value?.let { manga ->
                chapterHandler.updateChapter(manga, index, markPreviousRead = true)
                _chapters.value = chapterHandler.getChapters(manga)
            }
        }
    }

    data class Params(val mangaId: Long)
}
