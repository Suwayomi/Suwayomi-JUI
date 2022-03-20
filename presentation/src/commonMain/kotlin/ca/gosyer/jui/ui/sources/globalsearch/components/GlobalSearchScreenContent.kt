/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.sources.globalsearch.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ca.gosyer.jui.data.library.model.DisplayMode
import ca.gosyer.jui.data.models.Manga
import ca.gosyer.jui.data.models.Source
import ca.gosyer.jui.i18n.MR
import ca.gosyer.jui.ui.base.components.localeToString
import ca.gosyer.jui.ui.base.navigation.Toolbar
import ca.gosyer.jui.ui.sources.globalsearch.GlobalSearchViewModel.Search
import ca.gosyer.jui.uicore.components.ErrorScreen
import ca.gosyer.jui.uicore.components.HorizontalScrollbar
import ca.gosyer.jui.uicore.components.VerticalScrollbar
import ca.gosyer.jui.uicore.components.rememberScrollbarAdapter
import ca.gosyer.jui.uicore.resources.stringResource

@Composable
fun GlobalSearchScreenContent(
    sources: List<Source>,
    results: SnapshotStateMap<Long, Search>,
    displayMode: DisplayMode,
    query: String,
    setQuery: (String) -> Unit,
    submitSearch: (String) -> Unit,
    onSourceClick: (Source) -> Unit,
    onMangaClick: (Manga) -> Unit
) {
    Scaffold(
        topBar = {
            Toolbar(
                name = stringResource(MR.strings.location_global_search),
                searchText = query,
                search = setQuery,
                searchSubmit = { submitSearch(query) }
            )
        }
    ) { padding ->
        Box(Modifier.padding(padding)) {
            val state = rememberLazyListState()
            LazyColumn(state = state) {
                val sourcesSuccess = sources.filter { results[it.id] is Search.Success }
                val loadingSources = sources.filter { results[it.id] == null }
                val failedSources = sources.filter { results[it.id] is Search.Failure }
                items(sourcesSuccess) {
                    GlobalSearchItem(
                        source = it,
                        search = results[it.id] ?: Search.Searching,
                        displayMode = displayMode,
                        onSourceClick = onSourceClick,
                        onMangaClick = onMangaClick
                    )
                }
                items(loadingSources) {
                    GlobalSearchItem(
                        source = it,
                        search = results[it.id] ?: Search.Searching,
                        displayMode = displayMode,
                        onSourceClick = onSourceClick,
                        onMangaClick = onMangaClick
                    )
                }
                items(failedSources) {
                    GlobalSearchItem(
                        source = it,
                        search = results[it.id] ?: Search.Searching,
                        displayMode = displayMode,
                        onSourceClick = onSourceClick,
                        onMangaClick = onMangaClick
                    )
                }
            }
            VerticalScrollbar(
                modifier = Modifier.align(Alignment.CenterEnd)
                    .fillMaxHeight()
                    .padding(horizontal = 4.dp, vertical = 8.dp),
                adapter = rememberScrollbarAdapter(state)
            )
        }
    }
}

@Composable
fun GlobalSearchItem(
    source: Source,
    search: Search,
    displayMode: DisplayMode,
    onSourceClick: (Source) -> Unit,
    onMangaClick: (Manga) -> Unit
) {
    Column {
        Row(
            Modifier.fillMaxWidth()
                .clickable { onSourceClick(source) }
                .padding(vertical = 8.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    source.name,
                    maxLines = 1,
                    fontSize = 16.sp
                )
                Text(
                    localeToString(source.displayLang),
                    maxLines = 1,
                    fontSize = 12.sp
                )
            }
            Icon(Icons.Rounded.ArrowForward, stringResource(MR.strings.action_search))
        }

        Spacer(Modifier.height(4.dp))
        when (search) {
            is Search.Failure -> Box(Modifier.fillMaxWidth().padding(vertical = 8.dp, horizontal = 16.dp), contentAlignment = Alignment.Center) {
                ErrorScreen(search.e)
            }
            Search.Searching -> Box(Modifier.fillMaxWidth().padding(vertical = 8.dp, horizontal = 16.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            is Search.Success -> Box(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp, horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                val state = rememberLazyListState()
                LazyRow(Modifier.fillMaxSize(), state) {
                    items(search.mangaPage.mangaList) {
                        if (displayMode == DisplayMode.ComfortableGrid) {
                            GlobalSearchMangaComfortableGridItem(
                                Modifier.clickable { onMangaClick(it) },
                                it,
                                it.inLibrary
                            )
                        } else {
                            GlobalSearchMangaCompactGridItem(
                                Modifier.clickable { onMangaClick(it) },
                                it,
                                it.inLibrary
                            )
                        }
                    }
                }
                HorizontalScrollbar(
                    rememberScrollbarAdapter(state),
                    Modifier.align(Alignment.BottomCenter)
                        .fillMaxWidth()
                )
            }
        }
    }
}
