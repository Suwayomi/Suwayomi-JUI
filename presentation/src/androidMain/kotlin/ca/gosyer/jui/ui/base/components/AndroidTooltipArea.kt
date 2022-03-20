/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.base.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset

actual interface TooltipPlacement

actual class CursorPoint actual constructor(
    offset: DpOffset,
    alignment: Alignment,
    windowMargin: Dp
) : TooltipPlacement

actual class ComponentRect actual constructor(
    anchor: Alignment,
    alignment: Alignment,
    offset: DpOffset
) : TooltipPlacement

@OptIn(ExperimentalFoundationApi::class)
@Composable
actual fun TooltipArea(
    tooltip: @Composable () -> Unit,
    modifier: Modifier,
    delayMillis: Int,
    tooltipPlacement: TooltipPlacement,
    content: @Composable () -> Unit
) {
    Box(Modifier) {
        content()
    }
}
