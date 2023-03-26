/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.base.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

actual typealias TooltipPlacement = androidx.compose.foundation.TooltipPlacement

actual typealias CursorPointImpl = androidx.compose.foundation.TooltipPlacement.CursorPoint

actual typealias ComponentRectImpl = androidx.compose.foundation.TooltipPlacement.ComponentRect

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
