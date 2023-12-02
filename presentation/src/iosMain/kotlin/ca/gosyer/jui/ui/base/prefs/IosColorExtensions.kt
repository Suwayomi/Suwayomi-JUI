/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.base.prefs

import androidx.compose.ui.graphics.Color
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import platform.CoreGraphics.CGFloatVar
import platform.UIKit.UIColor

fun Color.toUIColor() = UIColor(red = red.toDouble(), green = green.toDouble(), blue = blue.toDouble(), alpha = 1.0)

@OptIn(ExperimentalForeignApi::class)
internal actual fun Color.toHsv(): FloatArray =
    memScoped {
        val uiColor = toUIColor()
        val hue = alloc<CGFloatVar>()
        val saturation = alloc<CGFloatVar>()
        val brightness = alloc<CGFloatVar>()
        val alpha = alloc<CGFloatVar>()
        uiColor.getHue(hue.ptr, saturation.ptr, brightness.ptr, alpha.ptr)

        floatArrayOf(hue.value.toFloat(), saturation.value.toFloat(), brightness.value.toFloat())
    }

internal actual fun hexStringToColor(hex: String): Color? {
    return try {
        val i = hex.removePrefix("#").toInt(16)
        Color(i shr 16 and 0xFF, i shr 8 and 0xFF, i and 0xFF)
    } catch (e: Exception) {
        null
    }
}
