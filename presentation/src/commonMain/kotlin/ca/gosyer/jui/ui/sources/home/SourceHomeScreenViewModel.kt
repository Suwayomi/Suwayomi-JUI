/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.sources.home

import androidx.compose.runtime.Stable
import androidx.compose.ui.text.intl.Locale
import ca.gosyer.jui.core.lang.displayName
import ca.gosyer.jui.domain.source.model.Source
import ca.gosyer.jui.domain.source.service.CatalogPreferences
import ca.gosyer.jui.domain.source.service.SourceRepository
import ca.gosyer.jui.i18n.MR
import ca.gosyer.jui.ui.base.state.SavedStateHandle
import ca.gosyer.jui.ui.base.state.getStateFlow
import ca.gosyer.jui.uicore.vm.ContextWrapper
import ca.gosyer.jui.uicore.vm.ViewModel
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableSet
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
    private val sourceHandler: SourceRepository,
    catalogPreferences: CatalogPreferences,
    contextWrapper: ContextWrapper,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel(contextWrapper) {
    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val installedSources = MutableStateFlow(emptyList<Source>())

    private val _languages = catalogPreferences.languages().asStateFlow()
    val languages = _languages.asStateFlow()
        .map { it.toImmutableSet() }
        .stateIn(scope, SharingStarted.Eagerly, persistentSetOf())

    val sources = combine(installedSources, languages) { installedSources, languages ->
        val all = MR.strings.all.toPlatformString()
        val other = MR.strings.other.toPlatformString()
        installedSources
            .distinctBy { it.id }
            .filter {
                it.lang in languages || it.id == Source.LOCAL_SOURCE_ID
            }
            .groupBy(Source::displayLang)
            .mapValues {
                it.value.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER, Source::name))
                    .map(SourceUI::SourceItem)
            }
            .mapKeys { (key) ->
                when (key) {
                    "all" -> all
                    "other" -> other
                    else -> Locale(key).displayName
                }
            }
            .toList()
            .sortedWith(
                compareBy<Pair<String, *>> { (key) ->
                    when (key) {
                        all -> 1
                        other -> 3
                        else -> 2
                    }
                }.thenBy(String.CASE_INSENSITIVE_ORDER, Pair<String, *>::first)
            )
            .flatMap { (key, value) ->
                listOf(SourceUI.Header(key)) + value
            }
            .toImmutableList()
    }.stateIn(scope, SharingStarted.Eagerly, persistentListOf())

    val sourceLanguages = installedSources.map { sources ->
        sources.map { it.lang }.distinct().minus(Source.LOCAL_SOURCE_LANG)
            .toImmutableList()
    }.stateIn(scope, SharingStarted.Eagerly, persistentListOf())

    private val _query by savedStateHandle.getStateFlow { "" }
    val query = _query.asStateFlow()

    init {
        getSources()
    }

    private fun getSources() {
        sourceHandler.getSourceList()
            .onEach {
                installedSources.value = it
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

@Stable
sealed class SourceUI {
    @Stable
    data class Header(val header: String) : SourceUI()

    @Stable
    data class SourceItem(val source: Source) : SourceUI()
}
