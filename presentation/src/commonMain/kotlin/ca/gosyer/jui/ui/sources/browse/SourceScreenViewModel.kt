/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.sources.browse

import ca.gosyer.jui.domain.library.model.DisplayMode
import ca.gosyer.jui.domain.library.service.LibraryPreferences
import ca.gosyer.jui.domain.source.interactor.GetLatestManga
import ca.gosyer.jui.domain.source.interactor.GetMangaPage
import ca.gosyer.jui.domain.source.interactor.GetPopularManga
import ca.gosyer.jui.domain.source.interactor.GetSearchManga
import ca.gosyer.jui.domain.source.interactor.SourcePager
import ca.gosyer.jui.domain.source.model.MangaPage
import ca.gosyer.jui.domain.source.model.Source
import ca.gosyer.jui.domain.source.model.sourcefilters.SourceFilter
import ca.gosyer.jui.domain.source.service.CatalogPreferences
import ca.gosyer.jui.ui.base.state.SavedStateHandle
import ca.gosyer.jui.ui.base.state.getStateFlow
import ca.gosyer.jui.uicore.vm.ContextWrapper
import ca.gosyer.jui.uicore.vm.ViewModel
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject
import com.diamondedge.logging.logging

class SourceScreenViewModel(
    private val source: Source,
    private val getLatestManga: GetLatestManga,
    private val getPopularManga: GetPopularManga,
    private val getSearchManga: GetSearchManga,
    private val catalogPreferences: CatalogPreferences,
    private val libraryPreferences: LibraryPreferences,
    private val getSourcePager: (GetMangaPage) -> SourcePager,
    contextWrapper: ContextWrapper,
    private val savedStateHandle: SavedStateHandle,
    initialQuery: String?,
) : ViewModel(contextWrapper) {
    @Inject
    constructor(
        getLatestManga: GetLatestManga,
        getPopularManga: GetPopularManga,
        getSearchManga: GetSearchManga,
        catalogPreferences: CatalogPreferences,
        libraryPreferences: LibraryPreferences,
        getSourcePager: (GetMangaPage) -> SourcePager,
        contextWrapper: ContextWrapper,
        @Assisted savedStateHandle: SavedStateHandle,
        @Assisted params: Params,
    ) : this(
        params.source,
        getLatestManga,
        getPopularManga,
        getSearchManga,
        catalogPreferences,
        libraryPreferences,
        getSourcePager,
        contextWrapper,
        savedStateHandle,
        params.initialQuery,
    )

    val displayMode = catalogPreferences.displayMode().stateIn(scope)
    val gridColumns = libraryPreferences.gridColumns().stateIn(scope)
    val gridSize = libraryPreferences.gridSize().stateIn(scope)

    private val _isLatest by savedStateHandle.getStateFlow { false }
    val isLatest = _isLatest.asStateFlow()

    private val usingFilters by savedStateHandle.getStateFlow { false }
    private val filters = MutableStateFlow<List<SourceFilter>?>(null)

    private val _sourceSearchQuery by savedStateHandle.getStateFlow { initialQuery }
    val sourceSearchQuery = _sourceSearchQuery.asStateFlow()

    private val query = MutableStateFlow(sourceSearchQuery.value)

    private val pager = MutableStateFlow(getPager())

    val mangas = pager.flatMapLatest { it.mangas.map { mangas -> mangas.toImmutableList() } }
        .stateIn(scope, SharingStarted.Eagerly, persistentListOf())
    val loading = pager.flatMapLatest { it.loading }
        .stateIn(scope, SharingStarted.Eagerly, true)
    val hasNextPage = pager.flatMapLatest { it.hasNextPage }
        .stateIn(scope, SharingStarted.Eagerly, true)

    init {
        pager.value.loadNextPage()
    }

    fun loadNextPage() {
        pager.value.loadNextPage()
    }

    fun setMode(toLatest: Boolean) {
        if (isLatest.value != toLatest) {
            _isLatest.value = toLatest
            query.value = null
            updatePager()
        }
    }

    private fun getPager(): SourcePager {
        val fetcher: suspend (page: Int) -> MangaPage? = when {
            query.value != null || usingFilters.value -> {
                { page ->
                    getSearchManga.await(
                        sourceId = source.id,
                        page = page,
                        searchTerm = query.value,
                        filters = filters.value,
                        onError = { toast(it.message.orEmpty()) },
                    )
                }
            }

            isLatest.value -> {
                { page ->
                    getLatestManga.await(
                        source,
                        page,
                        onError = { toast(it.message.orEmpty()) },
                    )
                }
            }

            else -> {
                { page ->
                    getPopularManga.await(
                        source.id,
                        page,
                        onError = { toast(it.message.orEmpty()) },
                    )
                }
            }
        }

        return getSourcePager(fetcher)
    }

    private fun updatePager() {
        pager.value.cancel()
        pager.value = getPager()
        pager.value.loadNextPage()
    }

    fun startSearch(query: String?) {
        this.query.value = query
        updatePager()
    }

    fun setUsingFilters(usingFilters: Boolean) {
        this.usingFilters.value = usingFilters
    }

    fun search(query: String) {
        _sourceSearchQuery.value = query
    }

    fun updateFilters(filters: List<SourceFilter>) {
        this.filters.value = filters
    }

    fun submitSearch() {
        startSearch(sourceSearchQuery.value)
    }

    fun selectDisplayMode(displayMode: DisplayMode) {
        catalogPreferences.displayMode().set(displayMode)
    }

    data class Params(
        val source: Source,
        val initialQuery: String?,
    )

    override fun onDispose() {
        super.onDispose()
        pager.value.cancel()
    }

    private companion object {
        private val log = logging()
    }
}
