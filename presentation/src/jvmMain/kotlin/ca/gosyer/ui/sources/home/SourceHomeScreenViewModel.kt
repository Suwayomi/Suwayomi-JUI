/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.sources.home

import ca.gosyer.core.lang.throwIfCancellation
import ca.gosyer.core.logging.CKLogger
import ca.gosyer.data.catalog.CatalogPreferences
import ca.gosyer.data.models.Source
import ca.gosyer.data.server.interactions.SourceInteractionHandler
import ca.gosyer.uicore.vm.ContextWrapper
import ca.gosyer.uicore.vm.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Inject

class SourceHomeScreenViewModel @Inject constructor(
    private val sourceHandler: SourceInteractionHandler,
    catalogPreferences: CatalogPreferences,
    contextWrapper: ContextWrapper
) : ViewModel(contextWrapper) {
    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val installedSources = MutableStateFlow(emptyList<Source>())

    private val _languages = catalogPreferences.languages().asStateFlow()
    val languages = _languages.asStateFlow()

    val sources = combine(installedSources, languages) { installedSources, languages ->
        installedSources.filter {
            it.lang in languages || it.lang == Source.LOCAL_SOURCE_LANG
        }
    }.stateIn(scope, SharingStarted.Eagerly, emptyList())

    val sourceLanguages = installedSources.map { sources ->
        sources.map { it.lang }.toSet() - setOf(Source.LOCAL_SOURCE_LANG)
    }.stateIn(scope, SharingStarted.Eagerly, emptySet())


    init {
        getSources()
    }

    private fun getSources() {
        scope.launch {
            try {
                installedSources.value = sourceHandler.getSourceList()
            } catch (e: Exception) {
                e.throwIfCancellation()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun setEnabledLanguages(langs: Set<String>) {
        info { langs }
        _languages.value = langs
    }

    private companion object : CKLogger({})
}
