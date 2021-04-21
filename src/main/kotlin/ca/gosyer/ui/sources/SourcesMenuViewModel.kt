/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.sources

import ca.gosyer.data.catalog.CatalogPreferences
import ca.gosyer.data.models.Source
import ca.gosyer.data.server.ServerPreferences
import ca.gosyer.data.server.interactions.SourceInteractionHandler
import ca.gosyer.ui.base.vm.ViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mu.KotlinLogging
import javax.inject.Inject

class SourcesMenuViewModel @Inject constructor(
    private val sourceHandler: SourceInteractionHandler,
    serverPreferences: ServerPreferences,
    catalogPreferences: CatalogPreferences
): ViewModel() {
    private val logger = KotlinLogging.logger {}

    val serverUrl = serverPreferences.server().stateIn(scope)

    private val languages = catalogPreferences.languages().stateIn(scope)

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val _sources = MutableStateFlow(emptyList<Source>())
    val sources = _sources.asStateFlow()

    private val _sourceTabs = MutableStateFlow(mapOf<Long?, Source?>(null to null))
    val sourceTabs = _sourceTabs.asStateFlow()

    private val _selectedSourceTab = MutableStateFlow<Source?>(null)
    val selectedSourceTab = _selectedSourceTab.asStateFlow()

    init {
        getSources()
    }

    private fun getSources() {
        scope.launch {
            try {
                val sources = sourceHandler.getSourceList()
                logger.info { sources }
                _sources.value = sources.filter { it.lang in languages.value }
                logger.info { _sources.value }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun selectTab(source: Source?) {
        _selectedSourceTab.value = source
    }

    fun addTab(source: Source) {
        _sourceTabs.value += source.id to source
        selectTab(source)
    }

    fun closeTab(source: Source) {
        _sourceTabs.value -= source.id
        if (selectedSourceTab.value?.id == source.id) {
            _selectedSourceTab.value = null
        }
    }
}