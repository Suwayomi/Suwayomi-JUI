/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.library.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.Scaffold
import androidx.compose.material.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ca.gosyer.data.library.model.DisplayMode
import ca.gosyer.data.models.Category
import ca.gosyer.data.models.Manga
import ca.gosyer.i18n.MR
import ca.gosyer.ui.base.navigation.Toolbar
import ca.gosyer.uicore.components.LoadingScreen
import ca.gosyer.uicore.resources.stringResource
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.rememberPagerState

@Composable
fun LibraryScreenContent(
    categories: List<Category>,
    selectedCategoryIndex: Int,
    displayMode: DisplayMode,
    isLoading: Boolean,
    error: String?,
    query: String,
    updateQuery: (String) -> Unit,
    getLibraryForPage: @Composable (Long) -> State<List<Manga>>,
    onPageChanged: (Int) -> Unit,
    onClickManga: (Long) -> Unit,
    onRemoveMangaClicked: (Long) -> Unit
) {
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
                isLoading = isLoading,
                error = error,
                query = query,
                updateQuery = updateQuery,
                getLibraryForPage = getLibraryForPage,
                onPageChanged = onPageChanged,
                onClickManga = onClickManga,
                onRemoveMangaClicked = onRemoveMangaClicked
            )
        } else {
            ThinLibraryScreenContent(
                pagerState = pagerState,
                categories = categories,
                selectedCategoryIndex = selectedCategoryIndex,
                displayMode = displayMode,
                isLoading = isLoading,
                error = error,
                query = query,
                updateQuery = updateQuery,
                getLibraryForPage = getLibraryForPage,
                onPageChanged = onPageChanged,
                onClickManga = onClickManga,
                onRemoveMangaClicked = onRemoveMangaClicked
            )
        }
    }
}

@Composable
fun WideLibraryScreenContent(
    pagerState: PagerState,
    categories: List<Category>,
    selectedCategoryIndex: Int,
    displayMode: DisplayMode,
    isLoading: Boolean,
    error: String?,
    query: String,
    updateQuery: (String) -> Unit,
    getLibraryForPage: @Composable (Long) -> State<List<Manga>>,
    onPageChanged: (Int) -> Unit,
    onClickManga: (Long) -> Unit,
    onRemoveMangaClicked: (Long) -> Unit
) {
    Scaffold(
        topBar = {
            Column {
                Toolbar(
                    stringResource(MR.strings.location_library),
                    searchText = query,
                    search = updateQuery
                )
                LibraryTabs(
                    visible = true, // vm.showCategoryTabs,
                    pagerState = pagerState,
                    categories = categories,
                    selectedPage = selectedCategoryIndex,
                    onPageChanged = onPageChanged
                )
            }
        }
    ) {
        Box(Modifier.padding(it)) {
            if (categories.isEmpty()) {
                LoadingScreen(isLoading, errorMessage = error)
            } else {
                LibraryPager(
                    pagerState = pagerState,
                    categories = categories,
                    displayMode = displayMode,
                    getLibraryForPage = getLibraryForPage,
                    onClickManga = onClickManga,
                    onRemoveMangaClicked = onRemoveMangaClicked
                )
            }
        }
    }
}

@Composable
fun ThinLibraryScreenContent(
    pagerState: PagerState,
    categories: List<Category>,
    selectedCategoryIndex: Int,
    displayMode: DisplayMode,
    isLoading: Boolean,
    error: String?,
    query: String,
    updateQuery: (String) -> Unit,
    getLibraryForPage: @Composable (Long) -> State<List<Manga>>,
    onPageChanged: (Int) -> Unit,
    onClickManga: (Long) -> Unit,
    onRemoveMangaClicked: (Long) -> Unit
) {
    val sheetState = rememberBottomSheetScaffoldState()
    BottomSheetScaffold(
        scaffoldState = sheetState,
        topBar = {
            Column {
                Toolbar(
                    stringResource(MR.strings.location_library),
                    searchText = query,
                    search = updateQuery
                )
                LibraryTabs(
                    visible = true, // vm.showCategoryTabs,
                    pagerState = pagerState,
                    categories = categories,
                    selectedPage = selectedCategoryIndex,
                    onPageChanged = onPageChanged
                )
            }
        },
        sheetContent = {
            // LibrarySheetContent()
        },
        sheetPeekHeight = 0.dp
    ) {
        Box(Modifier.padding(it)) {
            if (categories.isEmpty()) {
                LoadingScreen(isLoading, errorMessage = error)
            } else {
                LibraryPager(
                    pagerState = pagerState,
                    categories = categories,
                    displayMode = displayMode,
                    getLibraryForPage = getLibraryForPage,
                    onClickManga = onClickManga,
                    onRemoveMangaClicked = onRemoveMangaClicked
                )
            }
        }
    }
}
