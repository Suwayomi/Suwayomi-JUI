/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.uicore.theme

import androidx.compose.ui.graphics.Color
import kotlin.math.abs
import kotlin.random.Random

object RandomColors {
    private val colors = arrayOf(
        Color(0xffe57373),
        Color(0xfff06292),
        Color(0xffba68c8),
        Color(0xff9575cd),
        Color(0xff7986cb),
        Color(0xff64b5f6),
        Color(0xff4fc3f7),
        Color(0xff4dd0e1),
        Color(0xff4db6ac),
        Color(0xff81c784),
        Color(0xffaed581),
        Color(0xffff8a65),
        Color(0xffd4e157),
        Color(0xffffd54f),
        Color(0xffffb74d),
        Color(0xffa1887f),
        Color(0xff90a4ae),
    )

    fun get(key: Any): Color = colors[abs(key.hashCode()) % colors.size]

    fun random(): Color = colors[Random.nextInt(colors.size)]
}
