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
    val height: Int? = null
) {
    fun get(): Pair<IntOffset, IntSize> {
        val offset = if (x != null && y != null) {
            IntOffset(x, y)
        } else {
            IntOffset.Zero
        }
        val size = IntSize(width ?: 800, height ?: 600)
        return offset to size
    }
}
