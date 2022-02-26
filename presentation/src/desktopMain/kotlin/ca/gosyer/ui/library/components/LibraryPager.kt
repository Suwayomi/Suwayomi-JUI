/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.library.components

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import ca.gosyer.data.library.model.DisplayMode
import ca.gosyer.data.models.Category
import ca.gosyer.data.models.Manga
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState

@Composable
fun LibraryPager(
    categories: List<Category>,
    displayMode: DisplayMode,
    selectedPage: Int,
    getLibraryForPage: @Composable (Long) -> State<List<Manga>>,
    onPageChanged: (Int) -> Unit,
    onClickManga: (Long) -> Unit,
    onRemoveMangaClicked: (Long) -> Unit
) {
    if (categories.isEmpty()) return

    val state = rememberPagerState(selectedPage)
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
    HorizontalPager(categories.size, state = state) {
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
