/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.uicore.components

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.ui.Modifier

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
    adapter, modifier, reverseLayout, style, interactionSource
)

@Composable
internal actual fun RealHorizontalScrollbar(
    adapter: ScrollbarAdapter,
    modifier: Modifier,
    reverseLayout: Boolean,
    style: ScrollbarStyle,
    interactionSource: MutableInteractionSource
) = androidx.compose.foundation.HorizontalScrollbar(
    adapter, modifier, reverseLayout, style, interactionSource
)

@Composable
actual fun rememberScrollbarAdapter(
    scrollState: ScrollState
): ScrollbarAdapter {
    return androidx.compose.foundation.rememberScrollbarAdapter(scrollState)
}

@Composable
actual fun rememberScrollbarAdapter(
    scrollState: LazyListState,
): ScrollbarAdapter {
    return androidx.compose.foundation.rememberScrollbarAdapter(scrollState)
}
