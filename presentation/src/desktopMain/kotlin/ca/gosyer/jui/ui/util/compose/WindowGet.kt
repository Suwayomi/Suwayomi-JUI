/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.util.compose

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowPosition
import ca.gosyer.jui.domain.ui.model.WindowSettings

data class WindowGet(
    val offset: WindowPosition,
    val size: DpSize,
    val placement: WindowPlacement,
) {
    companion object {
        fun from(windowSettings: WindowSettings): WindowGet {
            return with(windowSettings) {
                // Maximize and Fullscreen messes with the other parameters so set them to default
                if (maximized == true) {
                    return WindowGet(
                        WindowPosition.PlatformDefault,
                        DpSize(800.dp, 600.dp),
                        WindowPlacement.Maximized,
                    )
                } else if (fullscreen == true) {
                    return WindowGet(
                        WindowPosition.PlatformDefault,
                        DpSize(800.dp, 600.dp),
                        WindowPlacement.Fullscreen,
                    )
                }

                val offset = if (x != null && y != null) {
                    WindowPosition(x!!.dp, y!!.dp)
                } else {
                    WindowPosition.PlatformDefault
                }
                val size = DpSize((width ?: 800).dp, (height ?: 600).dp)
                WindowGet(
                    offset,
                    size,
                    WindowPlacement.Floating,
                )
            }
        }
    }
}
