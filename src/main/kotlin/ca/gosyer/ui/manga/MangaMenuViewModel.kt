/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.manga

import ca.gosyer.backend.models.Chapter
import ca.gosyer.backend.models.Manga
import ca.gosyer.backend.network.interactions.ChapterInteractionHandler
import ca.gosyer.backend.network.interactions.LibraryInteractionHandler
import ca.gosyer.backend.network.interactions.MangaInteractionHandler
import ca.gosyer.backend.preferences.PreferenceHelper
import ca.gosyer.ui.base.vm.ViewModel
import ca.gosyer.util.system.inject
import io.ktor.client.HttpClient
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MangaMenuViewModel : ViewModel() {
    private val preferences: PreferenceHelper by inject()
    private val httpClient: HttpClient by inject()

    val serverUrl = preferences.serverUrl.asStateFlow(scope)

    private val _manga = MutableStateFlow<Manga?>(null)
    val manga = _manga.asStateFlow()

    private val _chapters = MutableStateFlow(emptyList<Chapter>())
    val chapters = _chapters.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    fun init(manga: Manga) {
        _manga.value = manga
        init(manga.id)
    }

    fun init(mangaId: Long) {
        scope.launch {
            refreshMangaAsync(mangaId).await() to refreshChaptersAsync(mangaId).await()
            _isLoading.value = false
        }
    }

    private suspend fun refreshMangaAsync(mangaId: Long) = withContext(Dispatchers.IO) {
        async {
            try {
                _manga.value = MangaInteractionHandler(httpClient).getManga(mangaId)
            } catch (e: Exception) {
                if (e is CancellationException) throw e
            }
        }
    }

    suspend fun refreshChaptersAsync(mangaId: Long) = withContext(Dispatchers.IO) {
        async {
            try {
                _chapters.value = ChapterInteractionHandler(httpClient).getChapters(mangaId)
            } catch (e: Exception) {
                if (e is CancellationException) throw e
            }
        }
    }

    fun toggleFavorite() {
        scope.launch {
            manga.value?.let {
                if (it.inLibrary) {
                    LibraryInteractionHandler(httpClient).removeMangaFromLibrary(it)
                } else {
                    LibraryInteractionHandler(httpClient).addMangaToLibrary(it)
                }

                refreshMangaAsync(it.id).await()
            }
        }

    }
}