/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.util.compose

import androidx.compose.desktop.Window
import androidx.compose.desktop.WindowEvents
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.window.v1.MenuBar
import ca.gosyer.common.di.AppScope
import ca.gosyer.data.translation.XmlResourceBundle
import ca.gosyer.ui.base.resources.LocalResources
import ca.gosyer.ui.base.theme.AppTheme
import java.awt.image.BufferedImage

fun ThemedWindow(
    title: String = "JetpackDesktopWindow",
    size: IntSize = IntSize(800, 600),
    location: IntOffset = IntOffset.Zero,
    centered: Boolean = true,
    icon: BufferedImage? = null,
    menuBar: MenuBar? = null,
    undecorated: Boolean = false,
    resizable: Boolean = true,
    events: WindowEvents = WindowEvents(),
    onDismissRequest: (() -> Unit)? = null,
    content: @Composable () -> Unit = { }
) {
    val resources = AppScope.getInstance<XmlResourceBundle>()
    Window(title, size, location, centered, icon, menuBar, undecorated, resizable, events, onDismissRequest) {
        CompositionLocalProvider(
            LocalResources provides resources
        ) {
            AppTheme {
                content()
            }
        }
    }
}
