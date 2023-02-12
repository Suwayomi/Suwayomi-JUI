/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.sources.browse.filter

import ca.gosyer.jui.domain.source.interactor.GetFilterList
import ca.gosyer.jui.domain.source.interactor.SetSourceFilter
import ca.gosyer.jui.domain.source.model.sourcefilters.SourceFilter
import ca.gosyer.jui.ui.base.state.SavedStateHandle
import ca.gosyer.jui.ui.base.state.getStateFlow
import ca.gosyer.jui.ui.sources.browse.filter.model.SourceFiltersView
import ca.gosyer.jui.uicore.vm.ContextWrapper
import ca.gosyer.jui.uicore.vm.ViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.supervisorScope
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject
import org.lighthousegames.logging.logging

class SourceFiltersViewModel(
    private val sourceId: Long,
    private val getFilterList: GetFilterList,
    private val setSourceFilter: SetSourceFilter,
    contextWrapper: ContextWrapper,
    private val savedStateHandle: SavedStateHandle
) : ViewModel(contextWrapper) {
    @Inject constructor(
        getFilterList: GetFilterList,
        setSourceFilter: SetSourceFilter,
        contextWrapper: ContextWrapper,
        @Assisted savedStateHandle: SavedStateHandle,
        @Assisted params: Params
    ) : this(
        params.sourceId,
        getFilterList,
        setSourceFilter,
        contextWrapper,
        savedStateHandle
    )

    private val _loading = MutableStateFlow(true)
    val loading = _loading.asStateFlow()

    private val _filters = MutableStateFlow<ImmutableList<SourceFiltersView<*, *>>>(persistentListOf())
    val filters = _filters.asStateFlow()

    private val _showingFilters by savedStateHandle.getStateFlow { false }
    val showingFilters = _showingFilters.asStateFlow()

    private val _filterButtonEnabled = MutableStateFlow(false)
    val filterButtonEnabled = _filterButtonEnabled.asStateFlow()

    init {
        getFilters(initialLoad = savedStateHandle["initialLoad"] ?: true)
        savedStateHandle["initialLoad"] = false

        filters.mapLatest { settings ->
            _filterButtonEnabled.value = settings.isNotEmpty()
            supervisorScope {
                settings.forEach { filter ->
                    if (filter is SourceFiltersView.Group) {
                        filter.state.value.forEach { childFilter ->
                            childFilter.state.drop(1)
                                .filterNotNull()
                                .onEach {
                                    setSourceFilter.await(
                                        sourceId = sourceId,
                                        filterIndex = filter.index,
                                        childFilterIndex = childFilter.index,
                                        filter = it,
                                        onError = { toast(it.message.orEmpty()) }
                                    )
                                    getFilters()
                                }
                                .launchIn(this)
                        }
                    } else {
                        filter.state.drop(1).filterNotNull()
                            .onEach {
                                setSourceFilter.await(
                                    sourceId = sourceId,
                                    filterIndex = filter.index,
                                    filter = it,
                                    onError = { toast(it.message.orEmpty()) }
                                )
                                getFilters()
                            }
                            .launchIn(this)
                    }
                }
            }
        }
            .catch {
                log.warn(it) { "Error with filters" }
            }
            .launchIn(scope)
    }

    fun showingFilters(show: Boolean) {
        _showingFilters.value = show
    }

    private fun getFilters(initialLoad: Boolean = false) {
        getFilterList.asFlow(sourceId, reset = initialLoad)
            .onEach {
                _filters.value = it.toView()
                _loading.value = false
            }
            .catch {
                toast(it.message.orEmpty())
                log.warn(it) { "Error getting filters" }
                _loading.value = false
            }
            .launchIn(scope)
    }

    fun resetFilters() {
        getFilters(initialLoad = true)
    }

    data class Params(val sourceId: Long)

    private fun List<SourceFilter>.toView() = mapIndexed { index, sourcePreference ->
        SourceFiltersView(index, sourcePreference)
    }.toImmutableList()

    private companion object {
        private val log = logging()
    }
}
