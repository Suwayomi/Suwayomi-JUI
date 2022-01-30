/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.sources.components.filter

import ca.gosyer.core.lang.throwIfCancellation
import ca.gosyer.core.logging.CKLogger
import ca.gosyer.data.models.sourcefilters.SourceFilter
import ca.gosyer.data.server.interactions.SourceInteractionHandler
import ca.gosyer.uicore.vm.ViewModel
import ca.gosyer.ui.sources.components.filter.model.SourceFiltersView
import com.github.zsoltk.compose.savedinstancestate.Bundle
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Inject
import java.util.concurrent.CopyOnWriteArrayList

class SourceFiltersViewModel(
    private val bundle: Bundle,
    private val sourceId: Long,
    private val sourceHandler: SourceInteractionHandler
) : ViewModel() {
    @Inject constructor(
        sourceHandler: SourceInteractionHandler,
        params: Params,
    ) : this(
        params.bundle,
        params.sourceId,
        sourceHandler
    )

    private val _loading = MutableStateFlow(true)
    val loading = _loading.asStateFlow()

    private val _filters = MutableStateFlow<List<SourceFiltersView<*, *>>>(emptyList())
    val filters = _filters.asStateFlow()

    private val _resetFilters = MutableSharedFlow<Unit>()
    val resetFilters = _resetFilters.asSharedFlow()

    private val subscriptions: CopyOnWriteArrayList<Job> = CopyOnWriteArrayList()

    init {
        getFilters(initialLoad = !bundle.getBoolean(FILTERING, false))

        filters.onEach { settings ->
            subscriptions.forEach { it.cancel() }
            subscriptions.clear()
            subscriptions += settings.flatMap { filter ->
                if (filter is SourceFiltersView.Group) {
                    filter.state.value.map { childFilter ->
                        childFilter.state.drop(1).filterNotNull().onEach {
                            sourceHandler.setFilter(
                                sourceId,
                                filter.index,
                                childFilter.index,
                                it
                            )
                            getFilters()
                        }.launchIn(scope)
                    }
                } else {
                    filter.state.drop(1).filterNotNull().onEach {
                        sourceHandler.setFilter(sourceId, filter.index, it)
                        getFilters()
                    }.launchIn(scope)
                        .let { listOf(it) }
                }
            }
        }.launchIn(scope)
    }

    private fun getFilters(initialLoad: Boolean = false) {
        scope.launch {
            try {
                _filters.value = sourceHandler.getFilterList(sourceId, reset = initialLoad).toView()
                if (!initialLoad) {
                    bundle.putBoolean(FILTERING, true)
                } else {
                    _resetFilters.emit(Unit)
                }
            } catch (e: Exception) {
                e.throwIfCancellation()
            } finally {
                _loading.value = false
            }
        }
    }

    fun resetFilters() {
        scope.launch {
            bundle.remove(FILTERING)
            getFilters(initialLoad = true)
        }
    }

    data class Params(val bundle: Bundle, val sourceId: Long)

    private fun List<SourceFilter>.toView() = mapIndexed { index, sourcePreference ->
        SourceFiltersView(index, sourcePreference)
    }

    private companion object : CKLogger({}) {
        const val FILTERING = "filtering"
    }
}
