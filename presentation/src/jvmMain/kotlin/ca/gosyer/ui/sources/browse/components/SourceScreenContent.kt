/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.sources.browse.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.forEachGesture
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.BottomSheetState
import androidx.compose.material.BottomSheetValue
import androidx.compose.material.ExtendedFloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material.icons.rounded.NewReleases
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import ca.gosyer.data.models.Manga
import ca.gosyer.data.models.Source
import ca.gosyer.i18n.MR
import ca.gosyer.ui.base.components.VerticalScrollbar
import ca.gosyer.ui.base.components.rememberScrollbarAdapter
import ca.gosyer.ui.base.navigation.ActionItem
import ca.gosyer.ui.base.navigation.BackHandler
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

    BackHandler {
        onCloseSourceTabClick(source)
    }

    BackHandler(showingFilters) {
        setShowingFilters(false)
    }

    BoxWithConstraints {
        if (maxWidth > 720.dp) {
            SourceWideScreenContent(
                source = source,
                onMangaClick = onMangaClick,
                onCloseSourceTabClick = onCloseSourceTabClick,
                onSourceSettingsClick = onSourceSettingsClick,
                mangas = mangas,
                hasNextPage = hasNextPage,
                loading = loading,
                isLatest = isLatest,
                showLatestButton = showLatestButton,
                sourceSearchQuery = sourceSearchQuery,
                search = search,
                submitSearch = submitSearch,
                setMode = setMode,
                loadNextPage = loadNextPage,
                setUsingFilters = setUsingFilters,
                filters = filters,
                showingFilters = showingFilters,
                showFilterButton = showFilterButton,
                setShowingFilters = setShowingFilters,
                resetFiltersClicked = resetFiltersClicked
            )
        } else {
            SourceThinScreenContent(
                source = source,
                onMangaClick = onMangaClick,
                onCloseSourceTabClick = onCloseSourceTabClick,
                onSourceSettingsClick = onSourceSettingsClick,
                mangas = mangas,
                hasNextPage = hasNextPage,
                loading = loading,
                isLatest = isLatest,
                showLatestButton = showLatestButton,
                sourceSearchQuery = sourceSearchQuery,
                search = search,
                submitSearch = submitSearch,
                setMode = setMode,
                loadNextPage = loadNextPage,
                setUsingFilters = setUsingFilters,
                filters = filters,
                showingFilters = showingFilters,
                showFilterButton = showFilterButton,
                setShowingFilters = setShowingFilters,
                resetFiltersClicked = resetFiltersClicked
            )
        }
    }
}

@Composable
private fun SourceWideScreenContent(
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
    Scaffold(
        topBar = {
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
        },
    ) { padding ->
        Box(Modifier.padding(padding)) {
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
            AnimatedVisibility(
                showingFilters && !isLatest,
                enter = fadeIn() + slideInHorizontally(initialOffsetX = { it * 2 }),
                exit = fadeOut() + slideOutHorizontally(targetOffsetX = { it * 2 }),
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                SourceFiltersMenu(
                    modifier = Modifier.width(360.dp),
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
}

@Composable
private fun SourceThinScreenContent(
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
    val bottomSheetScaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = BottomSheetState(
            BottomSheetValue.Collapsed,
            confirmStateChange = {
                when (it) {
                    BottomSheetValue.Collapsed -> setShowingFilters(false)
                    BottomSheetValue.Expanded -> setShowingFilters(true)
                }
                false
            }
        )
    )
    LaunchedEffect(showingFilters) {
        if (showingFilters) {
            bottomSheetScaffoldState.bottomSheetState.expand()
        } else {
            bottomSheetScaffoldState.bottomSheetState.collapse()
        }
    }
    BottomSheetScaffold(
        scaffoldState = bottomSheetScaffoldState,
        topBar = {
            SourceToolbar(
                source = source,
                onCloseSourceTabClick = onCloseSourceTabClick,
                sourceSearchQuery = sourceSearchQuery,
                onSearch = search,
                onSubmitSearch = submitSearch,
                onSourceSettingsClick = onSourceSettingsClick,
                showFilterButton = false,
                showLatestButton = showLatestButton,
                isLatest = isLatest,
                showingFilters = showingFilters,
                onClickMode = setMode,
                onToggleFiltersClick = setShowingFilters,
            )
        },
        sheetContent = {
            SourceFiltersMenu(
                modifier = Modifier,
                filters = filters,
                onSearchClicked = {
                    setUsingFilters(true)
                    setShowingFilters(false)
                    submitSearch()
                },
                resetFiltersClicked = resetFiltersClicked
            )
        },
        sheetPeekHeight = 0.dp
    ) {  padding ->
        Box(Modifier.padding(padding)) {
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
            if (showFilterButton && !isLatest) {
                ExtendedFloatingActionButton(
                    text = {
                        Text(stringResource(MR.strings.filter_source))
                    },
                    onClick = {
                        setShowingFilters(true)
                    },
                    icon = {
                        Icon(
                            Icons.Rounded.FilterList,
                            stringResource(MR.strings.filter_source)
                        )
                    },
                    modifier = Modifier.align(Alignment.BottomEnd)
                        .padding(bottom = 16.dp, end = 16.dp)
                )
            }
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
            getActionItems(
                isConfigurable = source.isConfigurable,
                onSourceSettingsClick = {
                    onSourceSettingsClick(source.id)
                },
                isLatest = isLatest,
                showLatestButton = showLatestButton,
                showFilterButton = showFilterButton,
                onToggleFiltersClick = {
                    onToggleFiltersClick(!showingFilters)
                },
                onClickMode = {
                    onClickMode(!isLatest)
                }
            )
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

@Composable
@Stable
private fun getActionItems(
    isConfigurable: Boolean,
    onSourceSettingsClick: () -> Unit,
    isLatest: Boolean,
    showLatestButton: Boolean,
    showFilterButton: Boolean,
    onToggleFiltersClick: () -> Unit,
    onClickMode: () -> Unit
): List<ActionItem> {
    return listOfNotNull(
        if (isConfigurable) {
            ActionItem(
                name = stringResource(MR.strings.location_settings),
                icon = Icons.Rounded.Settings,
                doAction = onSourceSettingsClick
            )
        } else null,
        if (showFilterButton) {
            ActionItem(
                name = stringResource(MR.strings.filter_source),
                icon = Icons.Rounded.FilterList,
                doAction = onToggleFiltersClick,
                enabled = !isLatest
            )
        } else null,
        if (showLatestButton) {
            ActionItem(
                name = stringResource(
                    if (isLatest) {
                        MR.strings.move_to_browse
                    } else {
                        MR.strings.move_to_latest
                    }
                ),
                icon = if (isLatest) {
                    Icons.Rounded.Explore
                } else {
                    Icons.Rounded.NewReleases
                },
                doAction = onClickMode
            )
        } else null
    )
}
