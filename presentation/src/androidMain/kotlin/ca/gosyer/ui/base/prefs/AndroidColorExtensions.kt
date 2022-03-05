/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.base.prefs

import androidx.compose.ui.graphics.Color

internal actual fun Color.toHsv(): FloatArray {
    fun Float.toIntColor() = (this * 256).toInt()
    val result = floatArrayOf(0f, 0f, 0f)
    android.graphics.Color.RGBToHSV(red.toIntColor(), green.toIntColor(), blue.toIntColor(), result)
    return result
}

internal actual fun hexStringToColor(hex: String): Color? {
    return try {
        val color = android.graphics.Color.parseColor(hex)
        Color(color)
    } catch (e: Exception) {
        null
    }
}
