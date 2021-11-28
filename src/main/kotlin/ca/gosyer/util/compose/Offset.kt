/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.util.compose

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize

operator fun IntSize.contains(offset: IntOffset): Boolean {
    return offset.x <= width && offset.y <= height
}

operator fun Size.contains(offset: Offset): Boolean {
    return offset.x <= width && offset.y <= height
}
