/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.library

import ca.gosyer.data.library.LibraryPreferences
import ca.gosyer.data.models.Category
import ca.gosyer.data.models.Manga
import ca.gosyer.data.server.ServerPreferences
import ca.gosyer.data.server.interactions.CategoryInteractionHandler
import ca.gosyer.data.server.interactions.LibraryInteractionHandler
import ca.gosyer.ui.base.vm.ViewModel
import ca.gosyer.util.lang.throwIfCancellation
import ca.gosyer.util.lang.withDefaultContext
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

private typealias LibraryMap = MutableMap<Int, MutableStateFlow<List<Manga>>>
private data class Library(val categories: MutableStateFlow<List<Category>>, val mangaMap: LibraryMap)

private fun LibraryMap.getManga(order: Int) =
    getOrPut(order) { MutableStateFlow(emptyList()) }
private fun LibraryMap.setManga(order: Int, manga: List<Manga>) {
    getManga(order).value = manga
}

class LibraryScreenViewModel @Inject constructor(
    private val categoryHandler: CategoryInteractionHandler,
    private val libraryHandler: LibraryInteractionHandler,
    libraryPreferences: LibraryPreferences,
    serverPreferences: ServerPreferences,
) : ViewModel() {
    val serverUrl = serverPreferences.serverUrl().stateIn(scope)

    private val library = Library(MutableStateFlow(emptyList()), mutableMapOf())
    val categories = library.categories.asStateFlow()

    private val _selectedCategoryIndex = MutableStateFlow(0)
    val selectedCategoryIndex = _selectedCategoryIndex.asStateFlow()

    val displayMode = libraryPreferences.displayMode().stateIn(scope)

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    init {
        getLibrary()
    }

    private fun getLibrary() {
        scope.launch {
            _isLoading.value = true
            try {
                val categories = categoryHandler.getCategories()
                if (categories.isEmpty()) {
                    throw Exception("Library is empty")
                }
                library.categories.value = categories.sortedBy { it.order }
                updateCategories(categories)
            } catch (e: Exception) {
                e.throwIfCancellation()
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun setSelectedPage(page: Int) {
        _selectedCategoryIndex.value = page
    }

    fun getLibraryForCategoryIndex(index: Int): StateFlow<List<Manga>> {
        return library.mangaMap.getManga(index).asStateFlow()
    }

    private suspend fun updateCategories(categories: List<Category>) {
        withDefaultContext {
            categories.map {
                async {
                    library.mangaMap.setManga(it.order, categoryHandler.getMangaFromCategory(it))
                }
            }.awaitAll()
        }
    }

    private fun getCategoriesToUpdate(mangaId: Long): List<Category> {
        return library.mangaMap
            .filter { mangaMapEntry ->
                mangaMapEntry.value.value.firstOrNull { it.id == mangaId } != null
            }
            .map { library.categories.value[it.key] }
    }

    fun removeManga(mangaId: Long) {
        scope.launch {
            libraryHandler.removeMangaFromLibrary(mangaId)
            updateCategories(getCategoriesToUpdate(mangaId))
        }
    }
}
