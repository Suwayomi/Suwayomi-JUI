/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.base.components

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.ui.Modifier

expect interface ScrollbarAdapter

expect class ScrollbarStyle

expect val LocalScrollbarStyle: ProvidableCompositionLocal<ScrollbarStyle>

@Composable
expect fun VerticalScrollbar(
    adapter: ScrollbarAdapter,
    modifier: Modifier,
    reverseLayout: Boolean,
    style: ScrollbarStyle,
    interactionSource: MutableInteractionSource,
)


@Composable
expect fun rememberScrollbarAdapter(
    scrollState: ScrollState
): ScrollbarAdapter

@Composable
expect fun rememberScrollbarAdapter(
    scrollState: LazyListState,
): ScrollbarAdapter