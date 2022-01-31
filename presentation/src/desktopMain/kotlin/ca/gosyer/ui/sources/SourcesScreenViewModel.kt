/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.sources

import ca.gosyer.data.models.Source
import ca.gosyer.uicore.vm.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import me.tatarka.inject.annotations.Inject

class SourcesScreenViewModel @Inject constructor() : ViewModel() {
    private val _sourceTabs = MutableStateFlow<List<Source?>>(listOf(null))
    val sourceTabs = _sourceTabs.asStateFlow()

    private val _selectedSourceTab = MutableStateFlow<Source?>(null)
    val selectedSourceTab = _selectedSourceTab.asStateFlow()

    fun selectTab(source: Source?) {
        if (source !in _sourceTabs.value) {
            _sourceTabs.value += source
        }
        _selectedSourceTab.value = source
    }

    fun closeTab(source: Source) {
        _sourceTabs.value -= source
        if (selectedSourceTab.value?.id == source.id) {
            _selectedSourceTab.value = null
        }
    }
}
