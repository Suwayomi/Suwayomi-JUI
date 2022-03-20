/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.library.components

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import ca.gosyer.jui.data.library.model.DisplayMode
import ca.gosyer.jui.data.models.Category
import ca.gosyer.jui.data.models.Manga
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState

@Composable
fun LibraryPager(
    pagerState: PagerState,
    categories: List<Category>,
    displayMode: DisplayMode,
    gridColumns: Int,
    gridSize: Int,
    getLibraryForPage: @Composable (Long) -> State<List<Manga>>,
    onClickManga: (Long) -> Unit,
    onRemoveMangaClicked: (Long) -> Unit
) {
    if (categories.isEmpty()) return

    HorizontalPager(categories.size, state = pagerState) {
        val library by getLibraryForPage(categories[it].id)
        when (displayMode) {
            DisplayMode.CompactGrid -> LibraryMangaCompactGrid(
                library = library,
                gridColumns = gridColumns,
                gridSize = gridSize,
                onClickManga = onClickManga,
                onRemoveMangaClicked = onRemoveMangaClicked
            )
            DisplayMode.ComfortableGrid -> LibraryMangaComfortableGrid(
                library = library,
                gridColumns = gridColumns,
                gridSize = gridSize,
                onClickManga = onClickManga,
                onRemoveMangaClicked = onRemoveMangaClicked
            )
            DisplayMode.CoverOnlyGrid -> LibraryMangaCoverOnlyGrid(
                library = library,
                gridColumns = gridColumns,
                gridSize = gridSize,
                onClickManga = onClickManga,
                onRemoveMangaClicked = onRemoveMangaClicked
            )
            DisplayMode.List -> LibraryMangaList(
                library = library,
                onClickManga = onClickManga,
                onRemoveMangaClicked = onRemoveMangaClicked
            )
            else -> Box {}
        }
    }
}
