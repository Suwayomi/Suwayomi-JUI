/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.sources.browse.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.forEachGesture
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.ExtendedFloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material.icons.rounded.NewReleases
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.ViewModule
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import ca.gosyer.jui.domain.library.model.DisplayMode
import ca.gosyer.jui.domain.manga.model.Manga
import ca.gosyer.jui.domain.source.model.Source
import ca.gosyer.jui.i18n.MR
import ca.gosyer.jui.ui.base.navigation.ActionItem
import ca.gosyer.jui.ui.base.navigation.BackHandler
import ca.gosyer.jui.ui.base.navigation.Toolbar
import ca.gosyer.jui.ui.main.components.bottomNav
import ca.gosyer.jui.ui.sources.browse.filter.SourceFiltersMenu
import ca.gosyer.jui.ui.sources.browse.filter.model.SourceFiltersView
import ca.gosyer.jui.uicore.components.DropdownMenu
import ca.gosyer.jui.uicore.components.DropdownMenuItem
import ca.gosyer.jui.uicore.components.LoadingScreen
import ca.gosyer.jui.uicore.insets.navigationBars
import ca.gosyer.jui.uicore.insets.statusBars
import ca.gosyer.jui.uicore.resources.stringResource
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

@Composable
fun SourceScreenContent(
    source: Source,
    onMangaClick: (Long) -> Unit,
    onCloseSourceTabClick: (Source) -> Unit,
    onSourceSettingsClick: (Long) -> Unit,
    displayMode: DisplayMode,
    gridColumns: Int,
    gridSize: Int,
    mangas: ImmutableList<Manga>,
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
    onSelectDisplayMode: (DisplayMode) -> Unit,
    // filter
    filters: ImmutableList<SourceFiltersView<*, *>>,
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
                displayMode = displayMode,
                gridColumns = gridColumns,
                gridSize = gridSize,
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
                onSelectDisplayMode = onSelectDisplayMode,
                resetFiltersClicked = resetFiltersClicked
            )
        } else {
            SourceThinScreenContent(
                source = source,
                onMangaClick = onMangaClick,
                onCloseSourceTabClick = onCloseSourceTabClick,
                onSourceSettingsClick = onSourceSettingsClick,
                displayMode = displayMode,
                gridColumns = gridColumns,
                gridSize = gridSize,
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
                onSelectDisplayMode = onSelectDisplayMode,
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
    displayMode: DisplayMode,
    gridColumns: Int,
    gridSize: Int,
    mangas: ImmutableList<Manga>,
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
    filters: ImmutableList<SourceFiltersView<*, *>>,
    showingFilters: Boolean,
    showFilterButton: Boolean,
    setShowingFilters: (Boolean) -> Unit,
    onSelectDisplayMode: (DisplayMode) -> Unit,
    resetFiltersClicked: () -> Unit
) {
    Scaffold(
        modifier = Modifier.windowInsetsPadding(
            WindowInsets.statusBars.add(
                WindowInsets.navigationBars.only(WindowInsetsSides.Horizontal)
            )
        ),
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
                onSelectDisplayMode = onSelectDisplayMode
            )
        }
    ) { padding ->
        Box(Modifier.padding(padding)) {
            MangaTable(
                displayMode = displayMode,
                gridColumns = gridColumns,
                gridSize = gridSize,
                mangas = mangas,
                isLoading = loading,
                hasNextPage = hasNextPage,
                onLoadNextPage = loadNextPage,
                onMangaClick = onMangaClick
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
    displayMode: DisplayMode,
    gridColumns: Int,
    gridSize: Int,
    mangas: ImmutableList<Manga>,
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
    filters: ImmutableList<SourceFiltersView<*, *>>,
    showingFilters: Boolean,
    showFilterButton: Boolean,
    setShowingFilters: (Boolean) -> Unit,
    onSelectDisplayMode: (DisplayMode) -> Unit,
    resetFiltersClicked: () -> Unit
) {
    val bottomSheetState = rememberModalBottomSheetState(
        ModalBottomSheetValue.Hidden,
        confirmStateChange = {
            when (it) {
                ModalBottomSheetValue.Hidden -> setShowingFilters(false)
                ModalBottomSheetValue.Expanded,
                ModalBottomSheetValue.HalfExpanded -> setShowingFilters(true)
            }
            true
        }
    )
    LaunchedEffect(showingFilters) {
        if (showingFilters) {
            bottomSheetState.show()
        } else {
            bottomSheetState.hide()
        }
    }
    Scaffold(
        modifier = Modifier.windowInsetsPadding(
            WindowInsets.statusBars.add(
                WindowInsets.navigationBars.only(WindowInsetsSides.Horizontal)
            )
        ),
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
                onSelectDisplayMode = onSelectDisplayMode
            )
        }
    ) { padding ->
        ModalBottomSheetLayout(
            sheetState = bottomSheetState,
            modifier = Modifier.padding(padding),
            sheetContent = {
                SourceFiltersMenu(
                    modifier = Modifier.windowInsetsPadding(
                        WindowInsets.bottomNav.add(
                            WindowInsets.navigationBars.only(
                                WindowInsetsSides.Bottom
                            )
                        )
                    ),
                    filters = filters,
                    onSearchClicked = {
                        setUsingFilters(true)
                        setShowingFilters(false)
                        submitSearch()
                    },
                    resetFiltersClicked = resetFiltersClicked
                )
            }
        ) {
            Box {
                MangaTable(
                    displayMode = displayMode,
                    gridColumns = gridColumns,
                    gridSize = gridSize,
                    mangas = mangas,
                    isLoading = loading,
                    hasNextPage = hasNextPage,
                    onLoadNextPage = loadNextPage,
                    onMangaClick = onMangaClick
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
                            Text(stringResource(MR.strings.action_filter))
                        },
                        onClick = {
                            setShowingFilters(true)
                        },
                        icon = {
                            Icon(
                                Icons.Rounded.FilterList,
                                stringResource(MR.strings.action_filter)
                            )
                        },
                        modifier = Modifier.align(Alignment.BottomEnd)
                            .padding(bottom = 16.dp, end = 16.dp)
                            .windowInsetsPadding(
                                WindowInsets.bottomNav.add(
                                    WindowInsets.navigationBars.only(
                                        WindowInsetsSides.Bottom
                                    )
                                )
                            )
                    )
                }
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
    onToggleFiltersClick: (Boolean) -> Unit,
    onSelectDisplayMode: (DisplayMode) -> Unit
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
            var displayModeSelectOpen by remember { mutableStateOf(false) }
            DisplayModeSelect(
                isVisible = displayModeSelectOpen,
                onSelectDisplayMode = onSelectDisplayMode,
                onDismissRequest = { displayModeSelectOpen = false }
            )
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
                },
                openDisplayModeSelect = { displayModeSelectOpen = true }
            )
        }
    )
}

@Composable
fun DisplayModeSelect(
    isVisible: Boolean,
    onSelectDisplayMode: (DisplayMode) -> Unit,
    onDismissRequest: () -> Unit
) {
    DropdownMenu(
        isVisible,
        onDismissRequest
    ) {
        val list = DisplayMode.values().toList() - DisplayMode.CoverOnlyGrid
        list.fastForEach {
            DropdownMenuItem(
                onClick = {
                    onSelectDisplayMode(it)
                    onDismissRequest()
                }
            ) {
                Text(stringResource(it.res))
            }
        }
    }
}

@Composable
private fun MangaTable(
    displayMode: DisplayMode,
    gridColumns: Int,
    gridSize: Int,
    mangas: ImmutableList<Manga>,
    isLoading: Boolean = false,
    hasNextPage: Boolean = false,
    onLoadNextPage: () -> Unit,
    onMangaClick: (Long) -> Unit
) {
    if (isLoading || mangas.isEmpty()) {
        LoadingScreen(isLoading)
    } else {
        when (displayMode) {
            DisplayMode.CompactGrid -> SourceMangaCompactGrid(
                mangas = mangas,
                gridColumns = gridColumns,
                gridSize = gridSize,
                onClickManga = onMangaClick,
                hasNextPage = hasNextPage,
                onLoadNextPage = onLoadNextPage
            )
            DisplayMode.ComfortableGrid -> SourceMangaComfortableGrid(
                mangas = mangas,
                gridColumns = gridColumns,
                gridSize = gridSize,
                onClickManga = onMangaClick,
                hasNextPage = hasNextPage,
                onLoadNextPage = onLoadNextPage
            )
            DisplayMode.List -> SourceMangaList(
                mangas = mangas,
                onClickManga = onMangaClick,
                hasNextPage = hasNextPage,
                onLoadNextPage = onLoadNextPage
            )
            else -> Box {}
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
    onClickMode: () -> Unit,
    openDisplayModeSelect: () -> Unit
): ImmutableList<ActionItem> {
    return listOfNotNull(
        if (showFilterButton) {
            ActionItem(
                name = stringResource(MR.strings.action_filter),
                icon = Icons.Rounded.FilterList,
                doAction = onToggleFiltersClick,
                enabled = !isLatest
            )
        } else {
            null
        },
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
        } else {
            null
        },
        ActionItem(
            name = stringResource(MR.strings.display_mode),
            icon = Icons.Rounded.ViewModule,
            doAction = openDisplayModeSelect
        ),
        if (isConfigurable) {
            ActionItem(
                name = stringResource(MR.strings.location_settings),
                icon = Icons.Rounded.Settings,
                doAction = onSourceSettingsClick
            )
        } else {
            null
        }
    ).toImmutableList()
}
