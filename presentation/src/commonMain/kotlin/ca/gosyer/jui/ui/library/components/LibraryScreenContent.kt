/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.library.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.forEachGesture
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import ca.gosyer.jui.domain.base.WebsocketService
import ca.gosyer.jui.domain.category.model.Category
import ca.gosyer.jui.domain.library.model.DisplayMode
import ca.gosyer.jui.i18n.MR
import ca.gosyer.jui.ui.base.navigation.ActionItem
import ca.gosyer.jui.ui.base.navigation.BackHandler
import ca.gosyer.jui.ui.base.navigation.OverflowMode
import ca.gosyer.jui.ui.base.navigation.Toolbar
import ca.gosyer.jui.ui.library.CategoryState
import ca.gosyer.jui.ui.library.settings.LibrarySheet
import ca.gosyer.jui.ui.library.settings.LibrarySideMenu
import ca.gosyer.jui.uicore.components.LoadingScreen
import ca.gosyer.jui.uicore.insets.navigationBars
import ca.gosyer.jui.uicore.insets.statusBars
import ca.gosyer.jui.uicore.resources.stringResource
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.rememberPagerState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

@Composable
fun LibraryScreenContent(
    categories: ImmutableList<Category>,
    selectedCategoryIndex: Int,
    displayMode: DisplayMode,
    gridColumns: Int,
    gridSize: Int,
    isLoading: Boolean,
    error: String?,
    query: String,
    updateQuery: (String) -> Unit,
    getLibraryForPage: @Composable (Long) -> State<CategoryState>,
    onPageChanged: (Int) -> Unit,
    onClickManga: (Long) -> Unit,
    onRemoveMangaClicked: (Long) -> Unit,
    onUpdateLibrary: () -> Unit,
    showingMenu: Boolean,
    setShowingMenu: (Boolean) -> Unit,
    libraryFilters: @Composable () -> Unit,
    librarySort: @Composable () -> Unit,
    libraryDisplay: @Composable () -> Unit,
    showUnread: Boolean,
    showDownloaded: Boolean,
    showLanguage: Boolean,
    showLocal: Boolean,
    updateWebsocketStatus: WebsocketService.Status,
    restartLibraryUpdates: () -> Unit,
) {
    BackHandler(showingMenu) {
        setShowingMenu(false)
    }

    BoxWithConstraints {
        val pagerState = rememberPagerState(selectedCategoryIndex)
        LaunchedEffect(pagerState.isScrollInProgress to pagerState.currentPage) {
            if (!pagerState.isScrollInProgress && pagerState.currentPage != selectedCategoryIndex) {
                onPageChanged(pagerState.currentPage)
            }
        }
        LaunchedEffect(selectedCategoryIndex) {
            if (pagerState.currentPage != selectedCategoryIndex) {
                pagerState.animateScrollToPage(selectedCategoryIndex)
            }
        }
        if (maxWidth > 720.dp) {
            WideLibraryScreenContent(
                pagerState = pagerState,
                categories = categories,
                selectedCategoryIndex = selectedCategoryIndex,
                displayMode = displayMode,
                gridColumns = gridColumns,
                gridSize = gridSize,
                isLoading = isLoading,
                error = error,
                query = query,
                updateQuery = updateQuery,
                getLibraryForPage = getLibraryForPage,
                onPageChanged = onPageChanged,
                onClickManga = onClickManga,
                onRemoveMangaClicked = onRemoveMangaClicked,
                onUpdateLibrary = onUpdateLibrary,
                showingMenu = showingMenu,
                setShowingMenu = setShowingMenu,
                libraryFilters = libraryFilters,
                librarySort = librarySort,
                libraryDisplay = libraryDisplay,
                showUnread = showUnread,
                showDownloaded = showDownloaded,
                showLanguage = showLanguage,
                showLocal = showLocal,
            )
        } else {
            ThinLibraryScreenContent(
                pagerState = pagerState,
                categories = categories,
                selectedCategoryIndex = selectedCategoryIndex,
                displayMode = displayMode,
                gridColumns = gridColumns,
                gridSize = gridSize,
                isLoading = isLoading,
                error = error,
                query = query,
                updateQuery = updateQuery,
                getLibraryForPage = getLibraryForPage,
                onPageChanged = onPageChanged,
                onClickManga = onClickManga,
                onRemoveMangaClicked = onRemoveMangaClicked,
                onUpdateLibrary = onUpdateLibrary,
                showingSheet = showingMenu,
                setShowingSheet = setShowingMenu,
                libraryFilters = libraryFilters,
                librarySort = librarySort,
                libraryDisplay = libraryDisplay,
                showUnread = showUnread,
                showDownloaded = showDownloaded,
                showLanguage = showLanguage,
                showLocal = showLocal,
                updateWebsocketStatus = updateWebsocketStatus,
                restartLibraryUpdates = restartLibraryUpdates,
            )
        }
    }
}

@Composable
fun WideLibraryScreenContent(
    pagerState: PagerState,
    categories: ImmutableList<Category>,
    selectedCategoryIndex: Int,
    displayMode: DisplayMode,
    gridColumns: Int,
    gridSize: Int,
    isLoading: Boolean,
    error: String?,
    query: String,
    updateQuery: (String) -> Unit,
    getLibraryForPage: @Composable (Long) -> State<CategoryState>,
    onPageChanged: (Int) -> Unit,
    onClickManga: (Long) -> Unit,
    onRemoveMangaClicked: (Long) -> Unit,
    onUpdateLibrary: () -> Unit,
    showingMenu: Boolean,
    setShowingMenu: (Boolean) -> Unit,
    libraryFilters: @Composable () -> Unit,
    librarySort: @Composable () -> Unit,
    libraryDisplay: @Composable () -> Unit,
    showUnread: Boolean,
    showDownloaded: Boolean,
    showLanguage: Boolean,
    showLocal: Boolean,
) {
    Scaffold(
        modifier = Modifier.windowInsetsPadding(
            WindowInsets.statusBars.add(
                WindowInsets.navigationBars.only(WindowInsetsSides.Horizontal),
            ),
        ),
        topBar = {
            Column {
                Toolbar(
                    stringResource(MR.strings.location_library),
                    searchText = query,
                    search = updateQuery,
                    actions = {
                        getActionItems(
                            onToggleFiltersClick = { setShowingMenu(true) },
                            onUpdateLibrary = onUpdateLibrary,
                        )
                    },
                )
                LibraryTabs(
                    visible = true, // vm.showCategoryTabs,
                    pagerState = pagerState,
                    categories = categories,
                    selectedPage = selectedCategoryIndex,
                    onPageChanged = onPageChanged,
                )
            }
        },
    ) { padding ->
        Box(Modifier.padding(padding)) {
            if (categories.isEmpty()) {
                LoadingScreen(isLoading, errorMessage = error)
            } else {
                LibraryPager(
                    pagerState = pagerState,
                    categories = categories,
                    displayMode = displayMode,
                    gridColumns = gridColumns,
                    gridSize = gridSize,
                    getLibraryForPage = getLibraryForPage,
                    onClickManga = onClickManga,
                    onRemoveMangaClicked = onRemoveMangaClicked,
                    showUnread = showUnread,
                    showDownloaded = showDownloaded,
                    showLanguage = showLanguage,
                    showLocal = showLocal,
                )

                if (showingMenu) {
                    Box(
                        Modifier.fillMaxSize().pointerInput(isLoading) {
                            forEachGesture {
                                detectTapGestures {
                                    setShowingMenu(false)
                                }
                            }
                        },
                    )
                }
                AnimatedVisibility(
                    showingMenu,
                    enter = fadeIn() + slideInHorizontally(initialOffsetX = { it * 2 }),
                    exit = fadeOut() + slideOutHorizontally(targetOffsetX = { it * 2 }),
                    modifier = Modifier.align(Alignment.TopEnd),
                ) {
                    LibrarySideMenu(
                        libraryFilters = libraryFilters,
                        librarySort = librarySort,
                        libraryDisplay = libraryDisplay,
                    )
                }
            }
        }
    }
}

@Composable
fun ThinLibraryScreenContent(
    pagerState: PagerState,
    categories: ImmutableList<Category>,
    selectedCategoryIndex: Int,
    displayMode: DisplayMode,
    gridColumns: Int,
    gridSize: Int,
    isLoading: Boolean,
    error: String?,
    query: String,
    updateQuery: (String) -> Unit,
    getLibraryForPage: @Composable (Long) -> State<CategoryState>,
    onPageChanged: (Int) -> Unit,
    onClickManga: (Long) -> Unit,
    onRemoveMangaClicked: (Long) -> Unit,
    onUpdateLibrary: () -> Unit,
    showingSheet: Boolean,
    setShowingSheet: (Boolean) -> Unit,
    libraryFilters: @Composable () -> Unit,
    librarySort: @Composable () -> Unit,
    libraryDisplay: @Composable () -> Unit,
    showUnread: Boolean,
    showDownloaded: Boolean,
    showLanguage: Boolean,
    showLocal: Boolean,
    updateWebsocketStatus: WebsocketService.Status,
    restartLibraryUpdates: () -> Unit,
) {
    val bottomSheetState = rememberModalBottomSheetState(
        ModalBottomSheetValue.Hidden,
        confirmStateChange = {
            when (it) {
                ModalBottomSheetValue.Hidden -> setShowingSheet(false)
                ModalBottomSheetValue.Expanded,
                ModalBottomSheetValue.HalfExpanded,
                -> setShowingSheet(true)
            }
            true
        },
    )
    LaunchedEffect(showingSheet) {
        if (showingSheet) {
            bottomSheetState.show()
        } else {
            bottomSheetState.hide()
        }
    }
    Scaffold(
        modifier = Modifier.windowInsetsPadding(
            WindowInsets.statusBars.add(
                WindowInsets.navigationBars.only(WindowInsetsSides.Horizontal),
            ),
        ),
        topBar = {
            Column {
                Toolbar(
                    stringResource(MR.strings.location_library),
                    searchText = query,
                    search = updateQuery,
                    actions = {
                        getActionItems(
                            onToggleFiltersClick = { setShowingSheet(true) },
                            onUpdateLibrary = onUpdateLibrary,
                            updateWebsocketStatus = updateWebsocketStatus,
                            restartLibraryUpdates = restartLibraryUpdates,
                        )
                    },
                )
                LibraryTabs(
                    visible = true, // vm.showCategoryTabs,
                    pagerState = pagerState,
                    categories = categories,
                    selectedPage = selectedCategoryIndex,
                    onPageChanged = onPageChanged,
                )
            }
        },
    ) { padding ->
        ModalBottomSheetLayout(
            sheetState = bottomSheetState,
            modifier = Modifier.padding(padding),
            sheetContent = {
                LibrarySheet(
                    libraryFilters = libraryFilters,
                    librarySort = librarySort,
                    libraryDisplay = libraryDisplay,
                )
            },
        ) {
            if (categories.isEmpty()) {
                LoadingScreen(isLoading, errorMessage = error)
            } else {
                LibraryPager(
                    pagerState = pagerState,
                    categories = categories,
                    displayMode = displayMode,
                    gridColumns = gridColumns,
                    gridSize = gridSize,
                    getLibraryForPage = getLibraryForPage,
                    onClickManga = onClickManga,
                    onRemoveMangaClicked = onRemoveMangaClicked,
                    showUnread = showUnread,
                    showDownloaded = showDownloaded,
                    showLanguage = showLanguage,
                    showLocal = showLocal,
                )
            }
        }
    }
}

@Composable
@Stable
private fun getActionItems(
    onToggleFiltersClick: () -> Unit,
    onUpdateLibrary: () -> Unit,
    updateWebsocketStatus: WebsocketService.Status? = null,
    restartLibraryUpdates: (() -> Unit)? = null,
): ImmutableList<ActionItem> {
    return listOfNotNull(
        ActionItem(
            name = stringResource(MR.strings.action_filter),
            icon = Icons.Rounded.FilterList,
            doAction = onToggleFiltersClick,
        ),
        ActionItem(
            name = stringResource(MR.strings.action_update_library),
            icon = Icons.Rounded.Refresh,
            doAction = onUpdateLibrary,
        ),
        if (updateWebsocketStatus == WebsocketService.Status.STOPPED && restartLibraryUpdates != null) {
            ActionItem(
                name = stringResource(MR.strings.action_restart_library),
                overflowMode = OverflowMode.ALWAYS_OVERFLOW,
                doAction = restartLibraryUpdates,
            )
        } else {
            null
        },
    ).toImmutableList()
}
