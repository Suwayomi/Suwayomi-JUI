/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.sources.components

import ca.gosyer.data.models.Manga
import ca.gosyer.data.models.MangaPage
import ca.gosyer.data.models.Source
import ca.gosyer.data.server.interactions.SourceInteractionHandler
import ca.gosyer.ui.base.vm.ViewModel
import ca.gosyer.util.compose.saveBooleanInBundle
import ca.gosyer.util.compose.saveIntInBundle
import ca.gosyer.util.compose.saveObjectInBundle
import ca.gosyer.util.compose.saveStringInBundle
import ca.gosyer.util.lang.throwIfCancellation
import com.github.zsoltk.compose.savedinstancestate.Bundle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class SourceScreenViewModel(
    private val source: Source,
    private val bundle: Bundle,
    private val sourceHandler: SourceInteractionHandler
) : ViewModel() {

    @Inject constructor(
        params: Params,
        sourceHandler: SourceInteractionHandler
    ) : this(
        params.source,
        params.bundle,
        sourceHandler
    )

    private val _mangas = saveObjectInBundle(scope, bundle, MANGAS_KEY) { emptyList<Manga>() }
    val mangas = _mangas.asStateFlow()

    private val _hasNextPage = saveBooleanInBundle(scope, bundle, NEXT_PAGE_KEY, false)
    val hasNextPage = _hasNextPage.asStateFlow()

    private val _loading = MutableStateFlow(true)
    val loading = _loading.asStateFlow()

    private val _isLatest = saveBooleanInBundle(scope, bundle, IS_LATEST_KEY, false)
    val isLatest = _isLatest.asStateFlow()

    private val _filterButtonEnabled = saveBooleanInBundle(scope, bundle, SHOW_FILTERS, false)
    val filterButtonEnabled = _filterButtonEnabled.asStateFlow()

    private val _latestButtonEnabled = saveBooleanInBundle(scope, bundle, SHOW_LATEST, false)
    val latestButtonEnabled = _latestButtonEnabled.asStateFlow()

    private val _showingFilters = MutableStateFlow(false)
    val showingFilters = _showingFilters.asStateFlow()

    private val _usingFilters = MutableStateFlow(false)

    private val _sourceSearchQuery = MutableStateFlow<String?>(null)
    val sourceSearchQuery = _sourceSearchQuery.asStateFlow()

    private val _query = saveStringInBundle(scope, bundle, QUERY_KEY) { null }

    private val _pageNum = saveIntInBundle(scope, bundle, PAGE_NUM_KEY, 1)
    val pageNum = _pageNum.asStateFlow()

    init {
        scope.launch {
            try {
                if (bundle[MANGAS_KEY] == null) {
                    val (mangas, hasNextPage) = getPage()
                    _mangas.value = mangas
                    _hasNextPage.value = hasNextPage
                }
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

    private fun cleanBundle(removeMode: Boolean = true) {
        bundle.remove(MANGAS_KEY)
        bundle.remove(NEXT_PAGE_KEY)
        bundle.remove(PAGE_NUM_KEY)
        if (removeMode) {
            bundle.remove(IS_LATEST_KEY)
        }
        bundle.remove(QUERY_KEY)
    }

    fun setMode(toLatest: Boolean) {
        if (isLatest.value != toLatest) {
            cleanBundle()
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
        cleanBundle(false)
        _pageNum.value = 0
        _hasNextPage.value = true
        _loading.value = true
        _query.value = query
        _mangas.value = emptyList()
        loadNextPage()
    }

    fun showingFilters(show: Boolean) {
        _showingFilters.value = show
    }
    fun setUsingFilters(usingFilters: Boolean) {
        _usingFilters.value = usingFilters
    }
    fun enableFilters(enabled: Boolean) {
        _filterButtonEnabled.value = enabled
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

    data class Params(val source: Source, val bundle: Bundle)

    private companion object {
        const val MANGAS_KEY = "mangas"
        const val NEXT_PAGE_KEY = "next_page"
        const val PAGE_NUM_KEY = "page_num"
        const val IS_LATEST_KEY = "is_latest"
        const val SHOW_FILTERS = "show_filters"
        const val SHOW_LATEST = "show_latest"
        const val QUERY_KEY = "query"
    }
}
