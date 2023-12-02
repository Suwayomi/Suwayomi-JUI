/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

@file:Suppress("DEPRECATION")

package ca.gosyer.jui.ui.library.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.TabRowDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.util.fastForEachIndexed
import ca.gosyer.jui.i18n.MR
import ca.gosyer.jui.uicore.components.VerticalScrollbar
import ca.gosyer.jui.uicore.components.rememberScrollbarAdapter
import ca.gosyer.jui.uicore.components.scrollbarPadding
import ca.gosyer.jui.uicore.resources.stringResource
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.pagerTabIndicatorOffset
import dev.icerock.moko.resources.StringResource
import kotlinx.coroutines.launch

enum class LibrarySheetTabs(val res: StringResource) {
    FILTERS(MR.strings.action_filter),
    SORT(MR.strings.library_sort),
    DISPLAY(MR.strings.library_display),
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun LibrarySheet(
    libraryFilters: @Composable () -> Unit,
    librarySort: @Composable () -> Unit,
    libraryDisplay: @Composable () -> Unit,
) {
    val pagerState = rememberPagerState { LibrarySheetTabs.values().size }
    val selectedPage = pagerState.currentPage
    val scope = rememberCoroutineScope()
    Column(Modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = pagerState.currentPage,
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    Modifier.pagerTabIndicatorOffset(pagerState, tabPositions),
                )
            },
        ) {
            LibrarySheetTabs.values().asList().fastForEachIndexed { index, tab ->
                Tab(
                    selected = selectedPage == index,
                    onClick = {
                        scope.launch { pagerState.animateScrollToPage(index) }
                    },
                    text = { Text(stringResource(tab.res)) },
                )
            }
        }
        HorizontalPager(
            state = pagerState,
            verticalAlignment = Alignment.Top,
        ) {
            val scrollState = rememberScrollState()
            Box {
                Column(
                    Modifier.fillMaxWidth()
                        .verticalScroll(scrollState),
                ) {
                    when (it) {
                        LibrarySheetTabs.FILTERS.ordinal -> libraryFilters()
                        LibrarySheetTabs.SORT.ordinal -> librarySort()
                        LibrarySheetTabs.DISPLAY.ordinal -> libraryDisplay()
                    }
                }

                VerticalScrollbar(
                    rememberScrollbarAdapter(scrollState),
                    Modifier.align(Alignment.CenterEnd)
                        .fillMaxHeight()
                        .scrollbarPadding(),
                )
            }
        }
    }
}
