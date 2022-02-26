/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.base.theme

import androidx.compose.runtime.Composable
import ca.gosyer.ui.base.components.ScrollbarStyle

actual object ThemeScrollbarStyle {
    @Composable
    actual fun getScrollbarStyle(): ScrollbarStyle {
        return ScrollbarStyle()
    }
}
