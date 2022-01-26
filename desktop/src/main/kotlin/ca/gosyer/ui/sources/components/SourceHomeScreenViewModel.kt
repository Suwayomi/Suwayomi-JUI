/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.sources.components

import ca.gosyer.core.lang.throwIfCancellation
import ca.gosyer.data.catalog.CatalogPreferences
import ca.gosyer.data.models.Source
import ca.gosyer.data.server.interactions.SourceInteractionHandler
import ca.gosyer.ui.base.vm.ViewModel
import ca.gosyer.util.system.CKLogger
import com.github.zsoltk.compose.savedinstancestate.Bundle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class SourceHomeScreenViewModel @Inject constructor(
    private val bundle: Bundle,
    private val sourceHandler: SourceInteractionHandler,
    catalogPreferences: CatalogPreferences
) : ViewModel() {
    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val _languages = catalogPreferences.languages().asStateFlow()
    val languages = _languages.asStateFlow()

    private val _sources = MutableStateFlow(emptyList<Source>())
    val sources = _sources.asStateFlow()

    private var installedSources = emptyList<Source>()

    init {
        getSources()
    }

    private fun getSources() {
        scope.launch {
            try {
                installedSources = sourceHandler.getSourceList()
                setSources(_languages.value)
                info { _sources.value }
            } catch (e: Exception) {
                e.throwIfCancellation()
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun setSources(langs: Set<String>) {
        _sources.value = installedSources.filter { it.lang in langs || it.lang == Source.LOCAL_SOURCE_LANG }
    }

    fun getSourceLanguages(): Set<String> {
        return installedSources.map { it.lang }.toSet() - setOf(Source.LOCAL_SOURCE_LANG)
    }

    fun setEnabledLanguages(langs: Set<String>) {
        info { langs }
        _languages.value = langs
        setSources(langs)
    }

    private companion object : CKLogger({})
}
