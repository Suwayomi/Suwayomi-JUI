/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.sources.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material.icons.rounded.NewReleases
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.unit.dp
import ca.gosyer.data.models.Manga
import ca.gosyer.data.models.Source
import ca.gosyer.ui.base.components.LoadingScreen
import ca.gosyer.ui.base.components.MangaGridItem
import ca.gosyer.ui.base.components.TextActionIcon
import ca.gosyer.ui.base.components.Toolbar
import ca.gosyer.ui.base.resources.stringResource
import ca.gosyer.ui.base.vm.viewModel
import ca.gosyer.ui.sources.components.filter.SourceFiltersMenu
import ca.gosyer.util.compose.persistentLazyListState
import com.github.zsoltk.compose.savedinstancestate.Bundle
import io.kamel.image.lazyPainterResource

@Composable
fun SourceScreen(
    bundle: Bundle,
    source: Source,
    onMangaClick: (Long) -> Unit,
    onCloseSourceTabClick: (Source) -> Unit,
    onSourceSettingsClick: (Long) -> Unit
) {
    val vm = viewModel<SourceScreenViewModel>(source.id) {
        SourceScreenViewModel.Params(source, bundle)
    }
    val mangas by vm.mangas.collectAsState()
    val hasNextPage by vm.hasNextPage.collectAsState()
    val loading by vm.loading.collectAsState()
    val isLatest by vm.isLatest.collectAsState()
    val showingFilters by vm.showingFilters.collectAsState()
    val showFilterButton by vm.filterButtonEnabled.collectAsState()
    val showLatestButton by vm.latestButtonEnabled.collectAsState()
    val sourceSearchQuery by vm.sourceSearchQuery.collectAsState()

    LaunchedEffect(vm to source) {
        vm.enableLatest(source.supportsLatest)
    }

    Column {
        SourceToolbar(
            source = source,
            onCloseSourceTabClick = onCloseSourceTabClick,
            sourceSearchQuery = sourceSearchQuery,
            onSearch = vm::search,
            onSubmitSearch = vm::submitSearch,
            onSourceSettingsClick = onSourceSettingsClick,
            showFilterButton = showFilterButton,
            showLatestButton = showLatestButton,
            isLatest = isLatest,
            showingFilters = showingFilters,
            onClickMode = vm::setMode,
            onToggleFiltersClick = vm::showingFilters,
        )
        Box {
            MangaTable(
                bundle = bundle,
                mangas = mangas,
                isLoading = loading,
                hasNextPage = hasNextPage,
                onLoadNextPage = vm::loadNextPage,
                onMangaClick = onMangaClick,
            )
            SourceFiltersMenu(
                bundle = bundle,
                modifier = Modifier.align(Alignment.TopEnd),
                sourceId = source.id,
                showFilters = showingFilters && !isLatest,
                onSearchClicked = {
                    vm.setUsingFilters(true)
                    vm.showingFilters(false)
                    vm.submitSearch()
                },
                onResetClicked = {
                    vm.setUsingFilters(false)
                    vm.showingFilters(false)
                    vm.submitSearch()
                },
                showFiltersButton = vm::enableFilters
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
                    stringResource("location_settings"),
                    Icons.Rounded.Settings
                )
            }
            if (showFilterButton) {
                TextActionIcon(
                    {
                        onToggleFiltersClick(!showingFilters)
                    },
                    stringResource("filter_source"),
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
                            "move_to_browse"
                        } else {
                            "move_to_latest"
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
    bundle: Bundle,
    mangas: List<Manga>,
    isLoading: Boolean = false,
    hasNextPage: Boolean = false,
    onLoadNextPage: () -> Unit,
    onMangaClick: (Long) -> Unit,
) {
    if (isLoading || mangas.isEmpty()) {
        LoadingScreen(isLoading)
    } else {
        val persistentState = persistentLazyListState(bundle)
        LazyVerticalGrid(GridCells.Adaptive(160.dp), state = persistentState) {
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
    }
}
