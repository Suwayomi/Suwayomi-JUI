/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.sources.browse.components

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.forEachGesture
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material.icons.rounded.NewReleases
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import ca.gosyer.data.models.Manga
import ca.gosyer.data.models.Source
import ca.gosyer.i18n.MR
import ca.gosyer.ui.base.navigation.TextActionIcon
import ca.gosyer.ui.base.navigation.Toolbar
import ca.gosyer.ui.sources.browse.filter.SourceFiltersMenu
import ca.gosyer.ui.sources.browse.filter.model.SourceFiltersView
import ca.gosyer.uicore.components.LoadingScreen
import ca.gosyer.uicore.components.MangaGridItem
import ca.gosyer.uicore.resources.stringResource
import io.kamel.image.lazyPainterResource

@Composable
fun SourceScreenContent(
    source: Source,
    onMangaClick: (Long) -> Unit,
    onCloseSourceTabClick: (Source) -> Unit,
    onSourceSettingsClick: (Long) -> Unit,
    mangas: List<Manga>,
    hasNextPage: Boolean,
    loading: Boolean,
    isLatest: Boolean,
    showLatestButton: Boolean,
    sourceSearchQuery: String?,
    enableLatest: (Boolean) -> Unit,
    search: (String) -> Unit,
    submitSearch: () -> Unit,
    setMode: (Boolean) -> Unit,
    loadNextPage: () -> Unit,
    setUsingFilters: (Boolean) -> Unit,
    // filter
    filters: List<SourceFiltersView<*, *>>,
    showingFilters: Boolean,
    showFilterButton: Boolean,
    setShowingFilters: (Boolean) -> Unit,
    resetFiltersClicked: () -> Unit
) {
    LaunchedEffect(source) {
        enableLatest(source.supportsLatest)
    }

    Column {
        SourceToolbar(
            source = source,
            onCloseSourceTabClick = onCloseSourceTabClick,
            sourceSearchQuery = sourceSearchQuery,
            onSearch = search,
            onSubmitSearch = submitSearch,
            onSourceSettingsClick = onSourceSettingsClick,
            showFilterButton = showFilterButton,
            showLatestButton = showLatestButton,
            isLatest = isLatest,
            showingFilters = showingFilters,
            onClickMode = setMode,
            onToggleFiltersClick = setShowingFilters,
        )
        Box {
            MangaTable(
                mangas = mangas,
                isLoading = loading,
                hasNextPage = hasNextPage,
                onLoadNextPage = loadNextPage,
                onMangaClick = onMangaClick,
            )
            if (showingFilters && !isLatest) {
                Box(
                    Modifier.fillMaxSize().pointerInput(loading) {
                        forEachGesture {
                            detectTapGestures {
                                setShowingFilters(false)
                            }
                        }
                    }
                )
            }
            SourceFiltersMenu(
                modifier = Modifier.align(Alignment.TopEnd),
                showFilters = showingFilters && !isLatest,
                filters = filters,
                onSearchClicked = {
                    setUsingFilters(true)
                    setShowingFilters(false)
                    submitSearch()
                },
                resetFiltersClicked = resetFiltersClicked
            )
        }
    }
}

@Composable
fun SourceToolbar(
    source: Source,
    onCloseSourceTabClick: (Source) -> Unit,
    sourceSearchQuery: String?,
    onSearch: (String) -> Unit,
    onSubmitSearch: () -> Unit,
    onSourceSettingsClick: (Long) -> Unit,
    showFilterButton: Boolean,
    showLatestButton: Boolean,
    isLatest: Boolean,
    showingFilters: Boolean,
    onClickMode: (Boolean) -> Unit,
    onToggleFiltersClick: (Boolean) -> Unit
) {
    Toolbar(
        source.name,
        closable = true,
        onClose = {
            onCloseSourceTabClick(source)
        },
        searchText = sourceSearchQuery,
        search = onSearch,
        searchSubmit = onSubmitSearch,
        actions = {
            if (source.isConfigurable) {
                TextActionIcon(
                    {
                        onSourceSettingsClick(source.id)
                    },
                    stringResource(MR.strings.location_settings),
                    Icons.Rounded.Settings
                )
            }
            if (showFilterButton) {
                TextActionIcon(
                    {
                        onToggleFiltersClick(!showingFilters)
                    },
                    stringResource(MR.strings.filter_source),
                    Icons.Rounded.FilterList,
                    !isLatest
                )
            }
            if (showLatestButton) {
                TextActionIcon(
                    {
                        onClickMode(!isLatest)
                    },
                    stringResource(
                        if (isLatest) {
                            MR.strings.move_to_browse
                        } else {
                            MR.strings.move_to_latest
                        }
                    ),
                    if (isLatest) {
                        Icons.Rounded.Explore
                    } else {
                        Icons.Rounded.NewReleases
                    }
                )
            }
        }
    )
}

@Composable
private fun MangaTable(
    mangas: List<Manga>,
    isLoading: Boolean = false,
    hasNextPage: Boolean = false,
    onLoadNextPage: () -> Unit,
    onMangaClick: (Long) -> Unit,
) {
    if (isLoading || mangas.isEmpty()) {
        LoadingScreen(isLoading)
    } else {
        val lazyListState = rememberLazyListState()
        Box {
            LazyVerticalGrid(GridCells.Adaptive(160.dp), state = lazyListState) {
                itemsIndexed(mangas) { index, manga ->
                    if (hasNextPage && index == mangas.lastIndex) {
                        LaunchedEffect(Unit) { onLoadNextPage() }
                    }
                    MangaGridItem(
                        title = manga.title,
                        cover = lazyPainterResource(manga, filterQuality = FilterQuality.Medium),
                        onClick = {
                            onMangaClick(manga.id)
                        }
                    )
                }
            }
            VerticalScrollbar(
                rememberScrollbarAdapter(lazyListState),
                Modifier.align(Alignment.CenterEnd)
                    .fillMaxHeight()
                    .padding(horizontal = 4.dp, vertical = 8.dp)
            )
        }
    }
}
