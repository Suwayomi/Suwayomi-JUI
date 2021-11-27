/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.sources

import ca.gosyer.data.models.Source
import ca.gosyer.ui.base.vm.ViewModel
import ca.gosyer.util.system.CKLogger
import com.github.zsoltk.compose.savedinstancestate.Bundle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

class SourcesMenuViewModel @Inject constructor(
    private val bundle: Bundle
) : ViewModel() {
    private val _sourceTabs = MutableStateFlow<List<Source?>>(listOf(null))
    val sourceTabs = _sourceTabs.asStateFlow()

    private val _selectedSourceTab = MutableStateFlow<Source?>(null)
    val selectedSourceTab = _selectedSourceTab.asStateFlow()

    init {
        _sourceTabs.drop(1)
            .onEach { sources ->
                bundle.putLongArray(SOURCE_TABS_KEY, sources.mapNotNull { it?.id }.toLongArray())
            }
            .launchIn(scope)

        _selectedSourceTab.drop(1)
            .onEach {
                if (it != null) {
                    bundle.putLong(SELECTED_SOURCE_TAB, it.id)
                } else {
                    bundle.remove(SELECTED_SOURCE_TAB)
                }
            }
            .launchIn(scope)
    }

    fun selectTab(source: Source?) {
        _selectedSourceTab.value = source
    }

    fun addTab(source: Source) {
        if (source !in _sourceTabs.value) {
            _sourceTabs.value += source
        }
        selectTab(source)
    }

    fun closeTab(source: Source) {
        _sourceTabs.value -= source
        if (selectedSourceTab.value?.id == source.id) {
            _selectedSourceTab.value = null
        }
        bundle.remove(source.id.toString())
    }

    fun setLoadedSources(sources: List<Source>) {
        val sourceTabs = bundle.getLongArray(SOURCE_TABS_KEY)
        if (sourceTabs != null) {
            _sourceTabs.value = listOf(null) + sourceTabs.toList()
                .mapNotNull { sourceId ->
                    sources.find { it.id == sourceId }
                }
            _selectedSourceTab.value = bundle.getLong(SELECTED_SOURCE_TAB, -1).let { id ->
                if (id != -1L) {
                    sources.find { it.id == id }
                } else null
            }
        }
    }

    private companion object : CKLogger({}) {
        const val SOURCE_TABS_KEY = "source_tabs"
        const val SELECTED_SOURCE_TAB = "selected_tab"
    }
}
