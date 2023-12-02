/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.sources.globalsearch

import androidx.compose.runtime.snapshots.SnapshotStateMap
import ca.gosyer.jui.core.lang.IO
import ca.gosyer.jui.domain.manga.model.Manga
import ca.gosyer.jui.domain.source.interactor.GetSearchManga
import ca.gosyer.jui.domain.source.interactor.GetSourceList
import ca.gosyer.jui.domain.source.model.Source
import ca.gosyer.jui.domain.source.service.CatalogPreferences
import ca.gosyer.jui.i18n.MR
import ca.gosyer.jui.ui.base.state.SavedStateHandle
import ca.gosyer.jui.ui.base.state.getStateFlow
import ca.gosyer.jui.uicore.vm.ContextWrapper
import ca.gosyer.jui.uicore.vm.ViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject
import org.lighthousegames.logging.logging

class GlobalSearchViewModel
    @Inject
    constructor(
        private val getSourceList: GetSourceList,
        private val getSearchManga: GetSearchManga,
        catalogPreferences: CatalogPreferences,
        contextWrapper: ContextWrapper,
        @Assisted private val savedStateHandle: SavedStateHandle,
        @Assisted params: Params,
    ) : ViewModel(contextWrapper) {
        private val _query by savedStateHandle.getStateFlow { params.initialQuery }
        val query = _query.asStateFlow()

        private val installedSources = MutableStateFlow(emptyList<Source>())

        private val languages = catalogPreferences.languages().stateIn(scope)
        val displayMode = catalogPreferences.displayMode().stateIn(scope)

        private val _isLoading = MutableStateFlow(true)
        val isLoading = _isLoading.asStateFlow()

        val sources = combine(installedSources, languages) { installedSources, languages ->
            installedSources.filter {
                it.lang in languages || it.id == Source.LOCAL_SOURCE_ID
            }.toImmutableList()
        }.stateIn(scope, SharingStarted.Eagerly, persistentListOf())

        private val search by savedStateHandle.getStateFlow { params.initialQuery }

        val results = SnapshotStateMap<Long, Search>()

        init {
            getSources()
            readySearch()
        }

        private fun getSources() {
            getSourceList.asFlow()
                .onEach { sources ->
                    installedSources.value = sources.sortedWith(
                        compareBy<Source, String>(String.CASE_INSENSITIVE_ORDER) { it.lang }
                            .thenBy(String.CASE_INSENSITIVE_ORDER) {
                                it.name
                            },
                    )
                    _isLoading.value = false
                }
                .catch {
                    toast(it.message.orEmpty())
                    log.warn(it) { "Error getting sources" }
                    _isLoading.value = false
                }
                .launchIn(scope)
        }

        private val semaphore = Semaphore(5)

        private fun readySearch() {
            search
                .combine(sources) { query, sources ->
                    query to sources
                }
                .mapLatest { (query, sources) ->
                    results.clear()
                    supervisorScope {
                        sources.map { source ->
                            async {
                                semaphore.withPermit {
                                    getSearchManga.asFlow(source, query, 1)
                                        .map {
                                            if (it.mangaList.isEmpty()) {
                                                Search.Failure(MR.strings.no_results_found.toPlatformString())
                                            } else {
                                                Search.Success(it.mangaList.toImmutableList())
                                            }
                                        }
                                        .catch {
                                            log.warn(it) { "Error getting search from ${source.displayName}" }
                                            emit(Search.Failure(it))
                                        }
                                        .onEach {
                                            results[source.id] = it
                                        }
                                        .collect()
                                }
                            }
                        }.awaitAll()
                    }
                }
                .catch {
                    log.warn(it) { "Error getting sources" }
                }
                .flowOn(Dispatchers.IO)
                .launchIn(scope)
        }

        fun setQuery(query: String) {
            _query.value = query
        }

        fun startSearch(query: String) {
            search.value = query
        }

        data class Params(
            val initialQuery: String,
        )

        sealed class Search {
            data object Searching : Search()

            data class Success(
                val mangaList: ImmutableList<Manga>,
            ) : Search()

            data class Failure(
                val e: String?,
            ) : Search() {
                constructor(e: Throwable) : this(e.message)
            }
        }

        private companion object {
            private val log = logging()
        }
    }
