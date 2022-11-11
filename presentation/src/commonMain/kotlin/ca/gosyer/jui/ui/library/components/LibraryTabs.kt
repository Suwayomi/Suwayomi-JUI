/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.library.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ScrollableTabRow
import androidx.compose.material.Tab
import androidx.compose.material.TabRowDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachIndexed
import ca.gosyer.jui.domain.category.model.Category
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.pagerTabIndicatorOffset
import kotlinx.collections.immutable.ImmutableList

@Composable
fun LibraryTabs(
    visible: Boolean,
    pagerState: PagerState,
    categories: ImmutableList<Category>,
    selectedPage: Int,
    onPageChanged: (Int) -> Unit
) {
    if (categories.isEmpty()) return

    AnimatedVisibility(
        visible = visible,
        enter = expandVertically(),
        exit = shrinkVertically()
    ) {
        Column {
            ScrollableTabRow(
                selectedTabIndex = selectedPage,
                backgroundColor = MaterialTheme.colors.surface,
                edgePadding = 0.dp,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        Modifier.pagerTabIndicatorOffset(pagerState, tabPositions)
                            .padding(horizontal = 8.dp)
                            .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp)),
                        height = 3.dp,
                        color = MaterialTheme.colors.primary
                    )
                },
                divider = {},
            ) {
                categories.fastForEachIndexed { i, category ->
                    Tab(
                        selected = selectedPage == i,
                        onClick = { onPageChanged(i) },
                        text = { Text(category.name) },
                        selectedContentColor = MaterialTheme.colors.primary,
                        unselectedContentColor = MaterialTheme.colors.onSurface,
                    )
                }
            }
            Divider()
        }
    }
}
