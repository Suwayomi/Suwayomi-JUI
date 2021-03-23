/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.library

import ca.gosyer.backend.models.Category
import ca.gosyer.backend.models.Manga
import ca.gosyer.backend.network.interactions.CategoryInteractionHandler
import ca.gosyer.backend.network.interactions.LibraryInteractionHandler
import ca.gosyer.backend.preferences.PreferenceHelper
import ca.gosyer.ui.base.vm.ViewModel
import ca.gosyer.util.system.inject
import io.ktor.client.HttpClient
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private typealias LibraryMap = MutableMap<Int, MutableStateFlow<List<Manga>>>
private data class Library(val categories: MutableStateFlow<List<Category>>, val mangaMap: LibraryMap)

private fun LibraryMap.getManga(order: Int) =
    getOrPut(order) { MutableStateFlow(emptyList()) }
private fun LibraryMap.setManga(order: Int, manga: List<Manga>) {
    getManga(order).value = manga
}

class LibraryScreenViewModel: ViewModel() {
    private val preferences: PreferenceHelper by inject()
    private val httpClient: HttpClient by inject()

    val serverUrl = preferences.serverUrl.asStateFlow(scope)

    private val library = Library(MutableStateFlow(emptyList()), mutableMapOf())
    val categories = library.categories.asStateFlow()

    private val _selectedCategoryIndex = MutableStateFlow(0)
    val selectedCategoryIndex = _selectedCategoryIndex.asStateFlow()

    val displayMode = preferences.libraryDisplay.asStateFlow(scope)

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    init {
        getLibrary()
    }

    private fun getLibrary() {
        scope.launch {
            _isLoading.value = true
            try {
                val categories = CategoryInteractionHandler(httpClient).getCategories()
                if (categories.isEmpty()) {
                    library.categories.value = listOf(defaultCategory)
                    library.mangaMap.setManga(defaultCategory.order, LibraryInteractionHandler(httpClient).getLibraryManga())
                } else {
                    library.categories.value = listOf(defaultCategory) + categories.sortedBy { it.order }
                    categories.map {
                        async {
                            library.mangaMap.setManga(it.order, CategoryInteractionHandler(httpClient).getMangaFromCategory(it.id))
                        }
                    }.awaitAll()
                    val mangaInCategories = library.mangaMap.flatMap { it.value.value }.map { it.id }.distinct()
                    library.mangaMap.setManga(defaultCategory.order, LibraryInteractionHandler(httpClient).getLibraryManga().filterNot { it.id in mangaInCategories })
                }
            } catch (e: Exception) {
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

    companion object {
        val defaultCategory = Category(0, 0, "Default",true)
    }
}


