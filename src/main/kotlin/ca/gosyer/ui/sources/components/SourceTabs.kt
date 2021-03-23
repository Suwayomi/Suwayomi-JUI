/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.sources.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ScrollableTabRow
import androidx.compose.material.Surface
import androidx.compose.material.Tab
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ca.gosyer.backend.models.Source

@Composable
fun SourceTopBar(
    openHome: () -> Unit,
    sources: Map<Long?, Source?>,
    tabSelected: Source?,
    onTabSelected: (Source?) -> Unit,
    onTabClosed: (Source) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        elevation = 2.dp,
        color = MaterialTheme.colors.surface
    ) {
        SourceTabBar(
            modifier = modifier,
            onHomeClicked = openHome
        ) { tabBarModifier ->
            SourceTabs(
                modifier = tabBarModifier,
                sources = sources,
                tabSelected = tabSelected,
                onTabSelected = { newTab -> onTabSelected(newTab) },
                onTabClosed = onTabClosed
            )
        }
    }

}

@Composable
fun SourceTabBar(
    modifier: Modifier = Modifier,
    onHomeClicked: () -> Unit,
    children: @Composable (Modifier) -> Unit
) {
        Row(modifier) {
            // Separate Row as the children shouldn't have the padding
            Box(Modifier.padding(4.dp).align(Alignment.CenterVertically)) {
                Image(
                    modifier = Modifier
                        .clickable(onClick = onHomeClicked),
                    imageVector = Icons.Filled.Home,
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(MaterialTheme.colors.onSurface)
                )
            }
            children(
                Modifier
                    .weight(1f)
                    .align(Alignment.CenterVertically)
            )

        }

}

@Composable
fun SourceTabs(
    modifier: Modifier = Modifier,
    sources: Map<Long?, Source?>,
    tabSelected: Source?,
    onTabSelected: (Source?) -> Unit,
    onTabClosed: (Source) -> Unit
) {
    ScrollableTabRow(
        selectedTabIndex = 1,//sources.indexOf(tabSelected).let { if (it == -1) 1 else it },
        modifier = modifier,
        contentColor = MaterialTheme.colors.onSurface,
        indicator = {

        },
        divider = {

        },
        backgroundColor = MaterialTheme.colors.surface
    ) {
        sources.forEach { (sourceId, source) ->
            val selected = sourceId == tabSelected?.id

            var rowModifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp)
            if (selected) {
                rowModifier =
                    Modifier
                        .border(BorderStroke(2.dp, MaterialTheme.colors.onSurface), RectangleShape)
                        .then(rowModifier)
            }

            Tab(
                modifier = Modifier.background(MaterialTheme.colors.surface),
                selected = selected,
                onClick = { onTabSelected(source) }
            ) {
                Row(rowModifier, verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = source?.name?.toUpperCase() ?: "Sources",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (source != null) {
                        Image(
                            modifier = Modifier
                                .clickable {
                                    onTabClosed(source)
                                },
                            imageVector = Icons.Filled.Close,
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(MaterialTheme.colors.onSurface)
                        )
                    }
                }
            }
        }
    }
}