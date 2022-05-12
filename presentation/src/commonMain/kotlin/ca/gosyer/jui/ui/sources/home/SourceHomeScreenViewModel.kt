/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.sources.home

import ca.gosyer.jui.data.catalog.CatalogPreferences
import ca.gosyer.jui.data.models.Source
import ca.gosyer.jui.data.server.interactions.SourceInteractionHandler
import ca.gosyer.jui.uicore.vm.ContextWrapper
import ca.gosyer.jui.uicore.vm.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import me.tatarka.inject.annotations.Inject
import org.lighthousegames.logging.logging

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
            it.lang in languages || it.id == Source.LOCAL_SOURCE_ID
        }
    }.stateIn(scope, SharingStarted.Eagerly, emptyList())

    val sourceLanguages = installedSources.map { sources ->
        sources.map { it.lang }.toSet() - Source.LOCAL_SOURCE_LANG
    }.stateIn(scope, SharingStarted.Eagerly, emptySet())

    private val _query = MutableStateFlow("")
    val query = _query.asStateFlow()

    init {
        getSources()
    }

    private fun getSources() {
        sourceHandler.getSourceList()
            .onEach {
                installedSources.value = it.sortedWith(
                    compareBy(String.CASE_INSENSITIVE_ORDER, Source::displayLang)
                        .thenBy(String.CASE_INSENSITIVE_ORDER, Source::name)
                )
                _isLoading.value = false
            }
            .catch {
                log.warn(it) { "Error getting sources" }
                _isLoading.value = false
            }
            .launchIn(scope)
    }

    fun setEnabledLanguages(langs: Set<String>) {
        log.info { langs }
        _languages.value = langs
    }

    fun setQuery(query: String) {
        _query.value = query
    }

    private companion object {
        private val log = logging()
    }
}
