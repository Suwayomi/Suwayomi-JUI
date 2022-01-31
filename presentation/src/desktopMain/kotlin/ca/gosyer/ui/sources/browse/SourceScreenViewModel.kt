/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.sources.browse

import ca.gosyer.core.lang.throwIfCancellation
import ca.gosyer.data.models.Manga
import ca.gosyer.data.models.MangaPage
import ca.gosyer.data.models.Source
import ca.gosyer.data.server.interactions.SourceInteractionHandler
import ca.gosyer.uicore.vm.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Inject

class SourceScreenViewModel(
    private val source: Source,
    private val sourceHandler: SourceInteractionHandler
) : ViewModel() {

    @Inject constructor(
        sourceHandler: SourceInteractionHandler,
        params: Params
    ) : this(
        params.source,
        sourceHandler
    )

    private val _mangas = MutableStateFlow(emptyList<Manga>())
    val mangas = _mangas.asStateFlow()

    private val _hasNextPage = MutableStateFlow(false)
    val hasNextPage = _hasNextPage.asStateFlow()

    private val _loading = MutableStateFlow(true)
    val loading = _loading.asStateFlow()

    private val _isLatest = MutableStateFlow(false)
    val isLatest = _isLatest.asStateFlow()

    private val _latestButtonEnabled = MutableStateFlow(false)
    val latestButtonEnabled = _latestButtonEnabled.asStateFlow()

    private val _usingFilters = MutableStateFlow(false)

    private val _sourceSearchQuery = MutableStateFlow<String?>(null)
    val sourceSearchQuery = _sourceSearchQuery.asStateFlow()

    private val _query = MutableStateFlow<String?>(null)

    private val _pageNum = MutableStateFlow(1)
    val pageNum = _pageNum.asStateFlow()

    init {
        scope.launch {
            try {
                val (mangas, hasNextPage) = getPage()
                _mangas.value = mangas
                _hasNextPage.value = hasNextPage
            } catch (e: Exception) {
                e.throwIfCancellation()
            } finally {
                _loading.value = false
            }
        }
    }

    fun loadNextPage() {
        scope.launch {
            val hasNextPage = hasNextPage.value
            val pageNum = pageNum.value
            try {
                _hasNextPage.value = false
                _pageNum.value++
                val page = getPage()
                _mangas.value += page.mangaList
                _hasNextPage.value = page.hasNextPage
            } catch (e: Exception) {
                _hasNextPage.value = hasNextPage
                _pageNum.value = pageNum
            } finally {
                _loading.value = false
            }
        }
    }

    fun setMode(toLatest: Boolean) {
        if (isLatest.value != toLatest) {
            _isLatest.value = toLatest
            // [loadNextPage] increments by 1
            _pageNum.value = 0
            _loading.value = true
            _query.value = null
            _mangas.value = emptyList()
            loadNextPage()
        }
    }

    private suspend fun getPage(): MangaPage {
        return when {
            isLatest.value -> sourceHandler.getLatestManga(source, pageNum.value)
            _query.value != null || _usingFilters.value -> sourceHandler.getSearchResults(source, _query.value.orEmpty(), pageNum.value)
            else -> sourceHandler.getPopularManga(source, pageNum.value)
        }
    }

    fun startSearch(query: String?) {
        _pageNum.value = 0
        _hasNextPage.value = true
        _loading.value = true
        _query.value = query
        _mangas.value = emptyList()
        loadNextPage()
    }

    fun setUsingFilters(usingFilters: Boolean) {
        _usingFilters.value = usingFilters
    }
    fun enableLatest(enabled: Boolean) {
        _latestButtonEnabled.value = enabled
    }

    fun search(query: String) {
        _sourceSearchQuery.value = query
    }
    fun submitSearch() {
        startSearch(sourceSearchQuery.value)
    }

    data class Params(val source: Source)
}
