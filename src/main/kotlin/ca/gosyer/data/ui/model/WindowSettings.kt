/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.data.ui.model

import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import kotlinx.serialization.Serializable

@Serializable
data class WindowSettings(
    val x: Int? = null,
    val y: Int? = null,
    val width: Int? = null,
    val height: Int? = null,
    val maximized: Boolean? = null
) {
    fun get(): WindowGet {
        if (maximized == true) {
            // Maximize messes with the other parameters so set them to default
            return WindowGet(
                IntOffset.Zero,
                IntSize(800, 600),
                true
            )
        }
        val offset = if (x != null && y != null) {
            IntOffset(x, y)
        } else {
            IntOffset.Zero
        }
        val size = IntSize(width ?: 800, height ?: 600)
        return WindowGet(
            offset,
            size,
            maximized ?: false
        )
    }

    data class WindowGet(
        val offset: IntOffset,
        val size: IntSize,
        val maximized: Boolean
    )
}
