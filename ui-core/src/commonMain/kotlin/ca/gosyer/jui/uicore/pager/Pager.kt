/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.uicore.pager

import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.snapping.SnapLayoutInfoProvider
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyListLayoutInfo
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastMaxBy
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlin.math.abs
import kotlin.math.absoluteValue
import kotlin.math.sign

@Composable
fun VerticalPager(
    count: Int,
    modifier: Modifier = Modifier,
    state: PagerState = rememberPagerState(),
    key: ((page: Int) -> Any)? = null,
    contentPadding: PaddingValues = PaddingValues(),
    horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    userScrollEnabled: Boolean = true,
    reverseLayout: Boolean = false,
    content: @Composable BoxScope.(page: Int) -> Unit,
) {
    Pager(
        count = count,
        modifier = modifier,
        state = state,
        isVertical = true,
        key = key,
        contentPadding = contentPadding,
        horizontalAlignment = horizontalAlignment,
        userScrollEnabled = userScrollEnabled,
        reverseLayout = reverseLayout,
        content = content,
    )
}

@Composable
fun HorizontalPager(
    count: Int,
    modifier: Modifier = Modifier,
    state: PagerState = rememberPagerState(),
    key: ((page: Int) -> Any)? = null,
    contentPadding: PaddingValues = PaddingValues(),
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    userScrollEnabled: Boolean = true,
    reverseLayout: Boolean = false,
    content: @Composable BoxScope.(page: Int) -> Unit,
) {
    Pager(
        count = count,
        modifier = modifier,
        state = state,
        isVertical = false,
        key = key,
        contentPadding = contentPadding,
        verticalAlignment = verticalAlignment,
        userScrollEnabled = userScrollEnabled,
        reverseLayout = reverseLayout,
        content = content,
    )
}

@Composable
private fun Pager(
    count: Int,
    modifier: Modifier,
    state: PagerState,
    isVertical: Boolean,
    key: ((page: Int) -> Any)?,
    contentPadding: PaddingValues,
    userScrollEnabled: Boolean,
    reverseLayout: Boolean,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    content: @Composable BoxScope.(page: Int) -> Unit,
) {
    LaunchedEffect(count) {
        state.updateCurrentPageBasedOnLazyListState()
    }

    LaunchedEffect(state) {
        snapshotFlow { state.mostVisiblePageLayoutInfo?.index }
            .distinctUntilChanged()
            .collect { state.updateCurrentPageBasedOnLazyListState() }
    }

    if (isVertical) {
        LazyColumn(
            modifier = modifier,
            state = state.lazyListState,
            contentPadding = contentPadding,
            horizontalAlignment = horizontalAlignment,
            verticalArrangement = Arrangement.aligned(verticalAlignment),
            userScrollEnabled = userScrollEnabled,
            reverseLayout = reverseLayout,
            flingBehavior = rememberLazyListSnapFlingBehavior(lazyListState = state.lazyListState),
        ) {
            items(
                count = count,
                key = key,
            ) { page ->
                Box(
                    modifier = Modifier
                        .fillParentMaxHeight()
                        .wrapContentSize(),
                ) {
                    content(this, page)
                }
            }
        }
    } else {
        LazyRow(
            modifier = modifier,
            state = state.lazyListState,
            contentPadding = contentPadding,
            verticalAlignment = verticalAlignment,
            horizontalArrangement = Arrangement.aligned(horizontalAlignment),
            userScrollEnabled = userScrollEnabled,
            reverseLayout = reverseLayout,
            flingBehavior = rememberLazyListSnapFlingBehavior(lazyListState = state.lazyListState),
        ) {
            items(
                count = count,
                key = key,
            ) { page ->
                Box(
                    modifier = Modifier
                        .fillParentMaxWidth()
                        .wrapContentSize(),
                ) {
                    content(this, page)
                }
            }
        }
    }
}

@Composable
fun rememberPagerState(initialPage: Int = 0) =
    rememberSaveable(saver = PagerState.Saver) {
        PagerState(currentPage = initialPage)
    }

@Stable
class PagerState(
    currentPage: Int = 0,
) {
    init {
        check(currentPage >= 0) { "currentPage cannot be less than zero" }
    }

    val lazyListState = LazyListState(firstVisibleItemIndex = currentPage)

    private var _currentPage by mutableStateOf(currentPage)

    var currentPage: Int
        get() = _currentPage
        set(value) {
            if (value != _currentPage) {
                _currentPage = value
            }
        }

    val mostVisiblePageLayoutInfo: LazyListItemInfo?
        get() {
            val layoutInfo = lazyListState.layoutInfo
            return layoutInfo.visibleItemsInfo.fastMaxBy {
                val start = maxOf(it.offset, 0)
                val end = minOf(
                    it.offset + it.size,
                    layoutInfo.viewportEndOffset - layoutInfo.afterContentPadding,
                )
                end - start
            }
        }

    fun updateCurrentPageBasedOnLazyListState() {
        mostVisiblePageLayoutInfo?.let {
            currentPage = it.index
        }
    }

    suspend fun animateScrollToPage(page: Int) {
        lazyListState.animateScrollToItem(index = page)
    }

    suspend fun scrollToPage(page: Int) {
        lazyListState.scrollToItem(index = page)
        updateCurrentPageBasedOnLazyListState()
    }

    companion object {
        val Saver: Saver<PagerState, *> = listSaver(
            save = { listOf(it.currentPage) },
            restore = { PagerState(it[0]) },
        )
    }
}

// https://android.googlesource.com/platform/frameworks/support/+/refs/changes/78/2160778/35/compose/foundation/foundation/src/commonMain/kotlin/androidx/compose/foundation/gestures/snapping/LazyListSnapLayoutInfoProvider.kt
private fun lazyListSnapLayoutInfoProvider(
    lazyListState: LazyListState,
    snapPosition: SnapPosition = SnapPosition.Center,
    density: State<Density>,
): SnapLayoutInfoProvider =
    object : SnapLayoutInfoProvider {

        private val layoutInfo: LazyListLayoutInfo
            get() = lazyListState.layoutInfo

        private val averageItemSize: Int
            get() {
                val layoutInfo = layoutInfo
                return if (layoutInfo.visibleItemsInfo.isEmpty()) {
                    0
                } else {
                    val numberOfItems = layoutInfo.visibleItemsInfo.size
                    layoutInfo.visibleItemsInfo.sumOf { it.size } / numberOfItems
                }
            }

        override fun calculateApproachOffset(velocity: Float, decayOffset: Float): Float {
            return (decayOffset.absoluteValue - averageItemSize).coerceAtLeast(0.0f) *
                decayOffset.sign
        }

        override fun calculateSnapOffset(velocity: Float): Float {
            var lowerBoundOffset = Float.NEGATIVE_INFINITY
            var upperBoundOffset = Float.POSITIVE_INFINITY

            layoutInfo.visibleItemsInfo.fastForEach { item ->
                val offset =
                    calculateDistanceToDesiredSnapPosition(
                        mainAxisViewPortSize = layoutInfo.singleAxisViewportSize,
                        beforeContentPadding = layoutInfo.beforeContentPadding,
                        afterContentPadding = layoutInfo.afterContentPadding,
                        itemSize = item.size,
                        itemOffset = item.offset,
                        itemIndex = item.index,
                        snapPosition = snapPosition,
                        itemCount = layoutInfo.totalItemsCount,
                    )

                // Find item that is closest to the center
                if (offset <= 0 && offset > lowerBoundOffset) {
                    lowerBoundOffset = offset
                }

                // Find item that is closest to center, but after it
                if (offset >= 0 && offset < upperBoundOffset) {
                    upperBoundOffset = offset
                }
            }

            return calculateFinalOffset(
                with(density.value) { calculateFinalSnappingItem(velocity) },
                lowerBoundOffset,
                upperBoundOffset,
            )
        }
    }

@Composable
private fun rememberLazyListSnapFlingBehavior(lazyListState: LazyListState): FlingBehavior {
    // return rememberSnapFlingBehavior(lazyListState)
    val density = rememberUpdatedState(LocalDensity.current)
    val snappingLayout = remember(lazyListState) { lazyListSnapLayoutInfoProvider(lazyListState, density = density) }
    return rememberSnapFlingBehavior(snappingLayout)
}

internal val LazyListLayoutInfo.singleAxisViewportSize: Int
    get() = if (orientation == Orientation.Vertical) viewportSize.height else viewportSize.width

@kotlin.jvm.JvmInline
internal value class FinalSnappingItem
internal constructor(@Suppress("unused") private val value: Int) {
    companion object {

        val ClosestItem: FinalSnappingItem = FinalSnappingItem(0)

        val NextItem: FinalSnappingItem = FinalSnappingItem(1)

        val PreviousItem: FinalSnappingItem = FinalSnappingItem(2)
    }
}

internal fun Density.calculateFinalSnappingItem(velocity: Float): FinalSnappingItem {
    return if (velocity.absoluteValue < 400.dp.toPx()) {
        FinalSnappingItem.ClosestItem
    } else {
        if (velocity > 0) FinalSnappingItem.NextItem else FinalSnappingItem.PreviousItem
    }
}

internal fun calculateFinalOffset(
    snappingOffset: FinalSnappingItem,
    lowerBound: Float,
    upperBound: Float,
): Float {
    fun Float.isValidDistance(): Boolean {
        return this != Float.POSITIVE_INFINITY && this != Float.NEGATIVE_INFINITY
    }

    val finalDistance =
        when (snappingOffset) {
            FinalSnappingItem.ClosestItem -> {
                if (abs(upperBound) <= abs(lowerBound)) {
                    upperBound
                } else {
                    lowerBound
                }
            }
            FinalSnappingItem.NextItem -> upperBound
            FinalSnappingItem.PreviousItem -> lowerBound
            else -> 0f
        }

    return if (finalDistance.isValidDistance()) {
        finalDistance
    } else {
        0f
    }
}

internal fun calculateDistanceToDesiredSnapPosition(
    mainAxisViewPortSize: Int,
    beforeContentPadding: Int,
    afterContentPadding: Int,
    itemSize: Int,
    itemOffset: Int,
    itemIndex: Int,
    snapPosition: SnapPosition,
    itemCount: Int,
): Float {
    val desiredDistance =
        with(snapPosition) {
                position(
                    mainAxisViewPortSize,
                    itemSize,
                    beforeContentPadding,
                    afterContentPadding,
                    itemIndex,
                    itemCount,
                )
            }
            .toFloat()

    return itemOffset - desiredDistance
}
