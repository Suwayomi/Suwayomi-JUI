/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.library.components

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import ca.gosyer.jui.domain.category.model.Category
import ca.gosyer.jui.domain.library.model.DisplayMode
import ca.gosyer.jui.ui.library.CategoryState
import ca.gosyer.jui.uicore.components.ErrorScreen
import ca.gosyer.jui.uicore.components.LoadingScreen
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState
import kotlinx.collections.immutable.ImmutableList

@Composable
fun LibraryPager(
    pagerState: PagerState,
    categories: ImmutableList<Category>,
    displayMode: DisplayMode,
    gridColumns: Int,
    gridSize: Int,
    getLibraryForPage: @Composable (Long) -> State<CategoryState>,
    onClickManga: (Long) -> Unit,
    onRemoveMangaClicked: (Long) -> Unit,
    showUnread: Boolean,
    showDownloaded: Boolean,
    showLanguage: Boolean,
    showLocal: Boolean,
) {
    if (categories.isEmpty()) return

    HorizontalPager(categories.size, state = pagerState) {
        when (val library = getLibraryForPage(categories[it].id).value) {
            CategoryState.Loading -> LoadingScreen()
            is CategoryState.Failed -> ErrorScreen(library.e.message)
            is CategoryState.Loaded -> LibraryLoadedPage(
                library = library,
                displayMode = displayMode,
                gridColumns = gridColumns,
                gridSize = gridSize,
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

@Composable
private fun LibraryLoadedPage(
    library: CategoryState.Loaded,
    displayMode: DisplayMode,
    gridColumns: Int,
    gridSize: Int,
    onClickManga: (Long) -> Unit,
    onRemoveMangaClicked: (Long) -> Unit,
    showUnread: Boolean,
    showDownloaded: Boolean,
    showLanguage: Boolean,
    showLocal: Boolean,
) {
    val items by library.items.collectAsState()
    when (displayMode) {
        DisplayMode.CompactGrid -> LibraryMangaCompactGrid(
            library = items,
            gridColumns = gridColumns,
            gridSize = gridSize,
            onClickManga = onClickManga,
            onRemoveMangaClicked = onRemoveMangaClicked,
            showUnread = showUnread,
            showDownloaded = showDownloaded,
            showLanguage = showLanguage,
            showLocal = showLocal,
        )
        DisplayMode.ComfortableGrid -> LibraryMangaComfortableGrid(
            library = items,
            gridColumns = gridColumns,
            gridSize = gridSize,
            onClickManga = onClickManga,
            onRemoveMangaClicked = onRemoveMangaClicked,
            showUnread = showUnread,
            showDownloaded = showDownloaded,
            showLanguage = showLanguage,
            showLocal = showLocal,
        )
        DisplayMode.CoverOnlyGrid -> LibraryMangaCoverOnlyGrid(
            library = items,
            gridColumns = gridColumns,
            gridSize = gridSize,
            onClickManga = onClickManga,
            onRemoveMangaClicked = onRemoveMangaClicked,
            showUnread = showUnread,
            showDownloaded = showDownloaded,
            showLanguage = showLanguage,
            showLocal = showLocal,
        )
        DisplayMode.List -> LibraryMangaList(
            library = items,
            onClickManga = onClickManga,
            onRemoveMangaClicked = onRemoveMangaClicked,
            showUnread = showUnread,
            showDownloaded = showDownloaded,
            showLanguage = showLanguage,
            showLocal = showLocal,
        )
        else -> Box {}
    }
}
