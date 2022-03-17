/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.util.compose

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

actual fun Color.toHexString(): String {
    return String.format("#%06X", (0xFFFFFF and toArgb()))
}

actual fun Color.toLong() = String.format("%06X", 0xFFFFFF and toArgb()).toLong(16)