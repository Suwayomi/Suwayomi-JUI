/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.library

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ScrollableTabRow
import androidx.compose.material.Tab
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import ca.gosyer.data.library.model.DisplayMode
import ca.gosyer.data.models.Category
import ca.gosyer.data.models.Manga
import ca.gosyer.ui.base.components.LoadingScreen
import ca.gosyer.ui.base.components.Pager
import ca.gosyer.ui.base.components.PagerState
import ca.gosyer.ui.base.vm.viewModel
import ca.gosyer.ui.manga.openMangaMenu
import ca.gosyer.util.compose.ThemedWindow

fun openLibraryMenu() {
    ThemedWindow {
        LibraryScreen()
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun LibraryScreen() {
    val vm = viewModel<LibraryScreenViewModel>()
    val categories by vm.categories.collectAsState()
    val selectedCategoryIndex by vm.selectedCategoryIndex.collectAsState()
    val displayMode by vm.displayMode.collectAsState()
    val isLoading by vm.isLoading.collectAsState()
    val serverUrl by vm.serverUrl.collectAsState()
    //val sheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)

    if (categories.isEmpty()) {
        LoadingScreen(isLoading)
    } else {

        /*ModalBottomSheetLayout(
        sheetState = sheetState,
        sheetContent = { *//*LibrarySheet()*//* }
    ) {*/
        Column {
            /*Toolbar(
                title = {
                    val text = if (vm.showCategoryTabs) {
                        stringResource(R.string.library_label)
                    } else {
                        vm.selectedCategory?.visibleName.orEmpty()
                    }
                    Text(text)
                },
                actions = {
                    IconButton(onClick = { scope.launch { sheetState.show() }}) {
                        Icon(Icons.Default.FilterList, contentDescription = null)
                    }
                }
            )*/
            LibraryTabs(
                visible = true, //vm.showCategoryTabs,
                categories = categories,
                selectedPage = selectedCategoryIndex,
                onPageChanged = vm::setSelectedPage
            )
            LibraryPager(
                categories = categories,
                displayMode = displayMode,
                selectedPage = selectedCategoryIndex,
                serverUrl = serverUrl,
                getLibraryForPage = { vm.getLibraryForCategoryIndex(it).collectAsState() },
                onPageChanged = { vm.setSelectedPage(it) },
                onClickManga = ::openMangaMenu
            )
        }
        // }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun LibraryTabs(
    visible: Boolean,
    categories: List<Category>,
    selectedPage: Int,
    onPageChanged: (Int) -> Unit
) {
    if (categories.isEmpty()) return

    AnimatedVisibility(
        visible = visible,
        enter = expandVertically(),
        exit = shrinkVertically()
    ) {
        ScrollableTabRow(
            selectedTabIndex = selectedPage,
            // backgroundColor = CustomColors.current.bars,
            // contentColor = CustomColors.current.onBars,
            edgePadding = 0.dp
        ) {
            categories.forEachIndexed { i, category ->
                Tab(
                    selected = selectedPage == i,
                    onClick = { onPageChanged(i) },
                    text = { Text(category.name) }
                )
            }
        }
    }
}

@Composable
private fun LibraryPager(
    categories: List<Category>,
    displayMode: DisplayMode,
    selectedPage: Int,
    serverUrl: String,
    getLibraryForPage: @Composable (Int) -> State<List<Manga>>,
    onPageChanged: (Int) -> Unit,
    onClickManga: (Manga) -> Unit
) {
    if (categories.isEmpty()) return

    val state = remember(categories.size, selectedPage) {
        PagerState(
            currentPage = selectedPage,
            minPage = 0,
            maxPage = categories.lastIndex
        )
    }
    LaunchedEffect(state.currentPage) {
        if (state.currentPage != selectedPage) {
            onPageChanged(state.currentPage)
        }
    }
    Pager(state = state, offscreenLimit = 1) {
        val library by getLibraryForPage(page)
        when (displayMode) {
            DisplayMode.CompactGrid -> LibraryMangaCompactGrid(
                library = library,
                serverUrl = serverUrl,
                onClickManga = onClickManga
            )
            /*DisplayMode.ComfortableGrid -> LibraryMangaComfortableGrid(
                library = library,
                onClickManga = onClickManga
            )
            DisplayMode.List -> LibraryMangaList(
                library = library,
                onClickManga = onClickManga
            )*/
            else -> Box {}
        }
    }
}