/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.uicore.components

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.abs

actual typealias ScrollbarAdapter = androidx.compose.foundation.ScrollbarAdapter

actual typealias ScrollbarStyle = androidx.compose.foundation.ScrollbarStyle

actual val LocalScrollbarStyle: ProvidableCompositionLocal<ScrollbarStyle>
    get() = androidx.compose.foundation.LocalScrollbarStyle

@Composable
internal actual fun RealVerticalScrollbar(
    adapter: ScrollbarAdapter,
    modifier: Modifier,
    reverseLayout: Boolean,
    style: ScrollbarStyle,
    interactionSource: MutableInteractionSource
) = androidx.compose.foundation.VerticalScrollbar(
    adapter,
    modifier,
    reverseLayout,
    style,
    interactionSource
)

@Composable
internal actual fun RealHorizontalScrollbar(
    adapter: ScrollbarAdapter,
    modifier: Modifier,
    reverseLayout: Boolean,
    style: ScrollbarStyle,
    interactionSource: MutableInteractionSource
) = androidx.compose.foundation.HorizontalScrollbar(
    adapter,
    modifier,
    reverseLayout,
    style,
    interactionSource
)

@Composable
actual fun rememberScrollbarAdapter(
    scrollState: ScrollState
): ScrollbarAdapter {
    return androidx.compose.foundation.rememberScrollbarAdapter(scrollState)
}

@Composable
actual fun rememberScrollbarAdapter(
    scrollState: LazyListState
): ScrollbarAdapter {
    return androidx.compose.foundation.rememberScrollbarAdapter(scrollState)
}

@Composable
actual fun rememberVerticalScrollbarAdapter(
    scrollState: LazyGridState,
    gridCells: GridCells,
    arrangement: Arrangement.Vertical?
): ScrollbarAdapter {
    val density = LocalDensity.current
    return remember(scrollState, gridCells, density, arrangement) {
        GridScrollbarAdapter(scrollState, gridCells, density, arrangement?.spacing ?: Dp.Hairline)
    }
}

@Composable
actual fun rememberHorizontalScrollbarAdapter(
    scrollState: LazyGridState,
    gridCells: GridCells,
    arrangement: Arrangement.Horizontal?
): ScrollbarAdapter {
    val density = LocalDensity.current
    return remember(scrollState, gridCells, density, arrangement) {
        GridScrollbarAdapter(scrollState, gridCells, density, arrangement?.spacing ?: Dp.Hairline)
    }
}

// TODO deal with item spacing
class GridScrollbarAdapter(
    private val scrollState: LazyGridState,
    private val gridCells: GridCells,
    private val density: Density,
    private val spacing: Dp
) : ScrollbarAdapter {
    override val scrollOffset: Float
        get() = (scrollState.firstVisibleItemIndex / itemsPerRow).coerceAtLeast(0) * averageItemSize + scrollState.firstVisibleItemScrollOffset

    override fun maxScrollOffset(containerSize: Int): Float {
        val size = with(gridCells) {
            with(density) {
                calculateCrossAxisCellSizes(containerSize, spacing.roundToPx()).size
            }
        }
        return (averageItemSize * (itemCount / size) - containerSize).coerceAtLeast(0f)
    }

    override suspend fun scrollTo(containerSize: Int, scrollOffset: Float) {
        val distance = scrollOffset - this@GridScrollbarAdapter.scrollOffset

        // if we scroll less than containerSize we need to use scrollBy function to avoid
        // undesirable scroll jumps (when an item size is different)
        //
        // if we scroll more than containerSize we should immediately jump to this position
        // without recreating all items between the current and the new position
        if (abs(distance) <= containerSize) {
            scrollState.scrollBy(distance)
        } else {
            snapTo(containerSize, scrollOffset)
        }
    }

    private suspend fun snapTo(containerSize: Int, scrollOffset: Float) {
        // In case of very big values, we can catch an overflow, so convert values to double and
        // coerce them
//        val averageItemSize = 26.000002f
//        val scrollOffsetCoerced = 2.54490608E8.toFloat()
//        val index = (scrollOffsetCoerced / averageItemSize).toInt() // 9788100
//        val offset = (scrollOffsetCoerced - index * averageItemSize) // -16.0
//        println(offset)

        val maximumValue = maxScrollOffset(containerSize).toDouble()
        val scrollOffsetCoerced = scrollOffset.toDouble().coerceIn(0.0, maximumValue)
        val averageItemSize = averageItemSize.toDouble()

        val index = (scrollOffsetCoerced / averageItemSize)
            .toInt()
            .div(
                with(gridCells) {
                    with(density) {
                        calculateCrossAxisCellSizes(containerSize, spacing.roundToPx()).size
                    }
                }
            )
            .coerceAtLeast(0)
            .coerceAtMost(itemCount - 1)

        val offset = (scrollOffsetCoerced - index * averageItemSize)
            .toInt()
            .coerceAtLeast(0)

        scrollState.scrollToItem(index = index, scrollOffset = offset)
    }

    private val itemCount get() = scrollState.layoutInfo.totalItemsCount

    private val averageItemSize: Float by derivedStateOf {
        scrollState
            .layoutInfo
            .visibleItemsInfo
            .asSequence()
            .map { it.size.height }
            .average()
            .toFloat()
    }

    private val itemsPerRow
        get() = with(gridCells) {
            with(density) {
                calculateCrossAxisCellSizes(
                    (scrollState.layoutInfo.viewportEndOffset - scrollState.layoutInfo.viewportStartOffset),
                    spacing.roundToPx()
                ).size
            }
        }
}

actual fun Modifier.scrollbarPadding() = padding(horizontal = 4.dp, vertical = 8.dp)
