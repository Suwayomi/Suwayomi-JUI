/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.base.theme

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.unit.dp
import ca.gosyer.jui.uicore.components.ScrollbarStyle

actual object ThemeScrollbarStyle {
    @Stable
    @Composable
    actual fun getScrollbarStyle(): ScrollbarStyle =
        androidx.compose.foundation.ScrollbarStyle(
            minimalHeight = 16.dp,
            thickness = 8.dp,
            shape = MaterialTheme.shapes.small,
            hoverDurationMillis = 300,
            unhoverColor = MaterialTheme.colors.onSurface.copy(alpha = 0.30f),
            hoverColor = MaterialTheme.colors.onSurface.copy(alpha = 0.70f),
        )
}
