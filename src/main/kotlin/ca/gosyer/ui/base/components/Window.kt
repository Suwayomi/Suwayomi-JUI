/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.base.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.window.FrameWindowScope
import javax.imageio.ImageIO

@Composable
fun FrameWindowScope.setIcon() {
    DisposableEffect(Unit) {
        window.iconImage = this::class.java.classLoader.getResourceAsStream("icon.png")!!.use {
            ImageIO.read(it)
        }
        onDispose {}
    }
}
