/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.library.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ScrollableTabRow
import androidx.compose.material.Tab
import androidx.compose.material.TabRowDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachIndexed
import ca.gosyer.data.models.Category
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.pagerTabIndicatorOffset

@Composable
fun LibraryTabs(
    visible: Boolean,
    pagerState: PagerState,
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
            edgePadding = 0.dp,
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    Modifier.pagerTabIndicatorOffset(pagerState, tabPositions)
                )
            }
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
