/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.uicore.components

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

expect interface ScrollbarAdapter

expect class ScrollbarStyle

expect val LocalScrollbarStyle: ProvidableCompositionLocal<ScrollbarStyle>

@Composable
internal expect fun RealVerticalScrollbar(
    adapter: ScrollbarAdapter,
    modifier: Modifier,
    reverseLayout: Boolean,
    style: ScrollbarStyle,
    interactionSource: MutableInteractionSource
)

@Composable
internal expect fun RealHorizontalScrollbar(
    adapter: ScrollbarAdapter,
    modifier: Modifier,
    reverseLayout: Boolean,
    style: ScrollbarStyle,
    interactionSource: MutableInteractionSource
)

@Composable
fun VerticalScrollbar(
    adapter: ScrollbarAdapter,
    modifier: Modifier = Modifier,
    reverseLayout: Boolean = false,
    style: ScrollbarStyle = LocalScrollbarStyle.current,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
) = RealVerticalScrollbar(adapter, modifier, reverseLayout, style, interactionSource)

@Composable
fun HorizontalScrollbar(
    adapter: ScrollbarAdapter,
    modifier: Modifier = Modifier,
    reverseLayout: Boolean = false,
    style: ScrollbarStyle = LocalScrollbarStyle.current,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
) = RealHorizontalScrollbar(adapter, modifier, reverseLayout, style, interactionSource)

@Composable
expect fun rememberScrollbarAdapter(
    scrollState: ScrollState
): ScrollbarAdapter

@Composable
expect fun rememberScrollbarAdapter(
    scrollState: LazyListState
): ScrollbarAdapter

@Composable
expect fun rememberVerticalScrollbarAdapter(
    scrollState: LazyGridState,
    gridCells: GridCells,
    arrangement: Arrangement.Vertical? = null
): ScrollbarAdapter

@Composable
expect fun rememberHorizontalScrollbarAdapter(
    scrollState: LazyGridState,
    gridCells: GridCells,
    arrangement: Arrangement.Horizontal? = null
): ScrollbarAdapter

expect fun Modifier.scrollbarPadding(): Modifier
