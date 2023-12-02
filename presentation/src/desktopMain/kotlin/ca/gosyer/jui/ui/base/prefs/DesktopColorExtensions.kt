/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.base.prefs

import androidx.compose.ui.graphics.Color

internal actual fun Color.toHsv(): FloatArray {
    fun Float.toIntColor() = (this * 256).toInt()
    val result = floatArrayOf(0f, 0f, 0f)
    java.awt.Color.RGBtoHSB(red.toIntColor(), green.toIntColor(), blue.toIntColor(), result)
    // HSB to HSV
    result[0] = result[0] * 360
    return result
}

internal actual fun hexStringToColor(hex: String): Color? =
    try {
        val color = java.awt.Color.decode(hex)
        Color(color.red, color.green, color.blue, color.alpha)
    } catch (e: Exception) {
        null
    }
