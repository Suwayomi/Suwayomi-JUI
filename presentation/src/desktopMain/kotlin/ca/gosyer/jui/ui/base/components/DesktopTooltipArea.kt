/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.base.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset

@OptIn(ExperimentalFoundationApi::class)
actual typealias TooltipPlacement = androidx.compose.foundation.TooltipPlacement

@OptIn(ExperimentalFoundationApi::class)
actual class CursorPointImpl actual constructor(
    offset: DpOffset,
    alignment: Alignment,
    windowMargin: Dp,
) : TooltipPlacement by androidx.compose.foundation.TooltipPlacement.CursorPoint(
    offset = offset,
    alignment = alignment,
    windowMargin = windowMargin
)

@OptIn(ExperimentalFoundationApi::class)
actual class ComponentRectImpl actual constructor(
    anchor: Alignment,
    alignment: Alignment,
    offset: DpOffset,
) : TooltipPlacement by androidx.compose.foundation.TooltipPlacement.ComponentRect(
    anchor = anchor,
    alignment = alignment,
    offset = offset
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal actual fun RealTooltipArea(
    tooltip: @Composable () -> Unit,
    modifier: Modifier,
    delayMillis: Int,
    tooltipPlacement: TooltipPlacement,
    content: @Composable () -> Unit,
) {
    androidx.compose.foundation.TooltipArea(
        tooltip,
        modifier,
        delayMillis,
        tooltipPlacement,
        content,
    )
}
