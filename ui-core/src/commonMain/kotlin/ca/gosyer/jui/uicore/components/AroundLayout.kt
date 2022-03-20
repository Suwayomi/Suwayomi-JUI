/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.uicore.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastMap
import androidx.compose.ui.util.fastMaxBy

@Composable
fun AroundLayout(
    modifier: Modifier = Modifier,
    startLayout: @Composable () -> Unit,
    endLayout: @Composable () -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    SubcomposeLayout(modifier) { constraints ->
        val layoutWidth = constraints.maxWidth
        val layoutHeight = constraints.maxHeight

        val looseConstraints = constraints.copy(minWidth = 0, minHeight = 0)

        layout(layoutWidth, layoutHeight) {
            val startLayoutPlaceables = subcompose(AroundLayoutContent.Start, startLayout).fastMap {
                it.measure(looseConstraints)
            }

            val startLayoutWidth = startLayoutPlaceables.fastMaxBy { it.width }?.width ?: 0

            val endLayoutPlaceables = subcompose(AroundLayoutContent.End, endLayout).fastMap {
                it.measure(looseConstraints)
            }

            val endLayoutWidth = endLayoutPlaceables.fastMaxBy { it.width }?.width ?: 0

            val bodyContentWidth = layoutWidth - startLayoutWidth

            val bodyContentPlaceables = subcompose(AroundLayoutContent.MainContent) {
                val innerPadding = PaddingValues(end = endLayoutWidth.toDp())
                content(innerPadding)
            }.fastMap { it.measure(looseConstraints.copy(maxWidth = bodyContentWidth)) }

            // Placing to control drawing order to match default elevation of each placeable

            bodyContentPlaceables.fastForEach {
                it.place(startLayoutWidth, 0)
            }
            startLayoutPlaceables.fastForEach {
                it.place(0, 0)
            }
            // The bottom bar is always at the bottom of the layout
            endLayoutPlaceables.fastForEach {
                it.place(layoutWidth - endLayoutWidth, 0)
            }
        }
    }
}

private enum class AroundLayoutContent { Start, MainContent, End }
