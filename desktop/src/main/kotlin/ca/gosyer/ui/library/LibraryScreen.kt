/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.library

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ScrollableTabRow
import androidx.compose.material.Surface
import androidx.compose.material.Tab
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachIndexed
import ca.gosyer.data.library.model.DisplayMode
import ca.gosyer.data.models.Category
import ca.gosyer.data.models.Manga
import ca.gosyer.desktop.build.BuildConfig
import ca.gosyer.i18n.MR
import ca.gosyer.ui.base.components.LoadingScreen
import ca.gosyer.ui.base.components.Toolbar
import ca.gosyer.ui.base.vm.viewModel
import ca.gosyer.ui.manga.openMangaMenu
import ca.gosyer.util.compose.ThemedWindow
import ca.gosyer.util.lang.launchApplication
import com.github.zsoltk.compose.savedinstancestate.Bundle
import com.github.zsoltk.compose.savedinstancestate.LocalSavedInstanceState
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.DelicateCoroutinesApi

@OptIn(DelicateCoroutinesApi::class)
fun openLibraryMenu() {
    launchApplication {
        ThemedWindow(::exitApplication, title = BuildConfig.NAME) {
            CompositionLocalProvider(
                LocalSavedInstanceState provides Bundle()
            ) {
                Surface {
                    LibraryScreen()
                }
            }
        }
    }
}

@Composable
fun LibraryScreen(onClickManga: (Long) -> Unit = ::openMangaMenu) {
    LibraryScreen(LocalSavedInstanceState.current, onClickManga)
}

@Composable
fun LibraryScreen(bundle: Bundle, onClickManga: (Long) -> Unit = ::openMangaMenu) {
    val vm = viewModel {
        instantiate<LibraryScreenViewModel>(bundle)
    }
    val categories by vm.categories.collectAsState()
    val selectedCategoryIndex by vm.selectedCategoryIndex.collectAsState()
    val displayMode by vm.displayMode.collectAsState()
    val isLoading by vm.isLoading.collectAsState()
    val error by vm.error.collectAsState()
    val query by vm.query.collectAsState()
    // val sheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)

    if (categories.isEmpty()) {
        LoadingScreen(isLoading, errorMessage = error)
    } else {
        /*ModalBottomSheetLayout(
            sheetState = sheetState,
            sheetContent = { *//*LibrarySheet()*//* }
        ) {*/
        Column(Modifier.fillMaxWidth()) {
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
                        Icon(Icons.Rounded.FilterList, contentDescription = null)
                    }
                }
            )*/
            Toolbar(
                stringResource(MR.strings.location_library),
                closable = false,
                searchText = query,
                search = vm::updateQuery
            )
            LibraryTabs(
                visible = true, // vm.showCategoryTabs,
                categories = categories,
                selectedPage = selectedCategoryIndex,
                onPageChanged = vm::setSelectedPage
            )
            LibraryPager(
                categories = categories,
                displayMode = displayMode,
                selectedPage = selectedCategoryIndex,
                getLibraryForPage = { vm.getLibraryForCategoryId(it).collectAsState() },
                onPageChanged = vm::setSelectedPage,
                onClickManga = onClickManga,
                onRemoveMangaClicked = vm::removeManga
            )
        }
        // }
    }
}

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
            backgroundColor = MaterialTheme.colors.surface,
            // contentColor = CustomColors.current.onBars,
            edgePadding = 0.dp
        ) {
            categories.fastForEachIndexed { i, category ->
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
    getLibraryForPage: @Composable (Long) -> State<List<Manga>>,
    onPageChanged: (Int) -> Unit,
    onClickManga: (Long) -> Unit,
    onRemoveMangaClicked: (Long) -> Unit
) {
    if (categories.isEmpty()) return

    val state = rememberPagerState(categories.size, selectedPage)
    LaunchedEffect(state.currentPage) {
        if (state.currentPage != selectedPage) {
            onPageChanged(state.currentPage)
        }
    }
    LaunchedEffect(selectedPage) {
        if (state.currentPage != selectedPage) {
            state.animateScrollToPage(selectedPage)
        }
    }
    HorizontalPager(state = state) {
        val library by getLibraryForPage(categories[it].id)
        when (displayMode) {
            DisplayMode.CompactGrid -> LibraryMangaCompactGrid(
                library = library,
                onClickManga = onClickManga,
                onRemoveMangaClicked = onRemoveMangaClicked
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
