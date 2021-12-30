/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.main.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.Notification
import androidx.compose.ui.window.Tray
import androidx.compose.ui.window.rememberTrayState
import ca.gosyer.build.BuildConfig
import ca.gosyer.data.translation.XmlResourceBundle
import ca.gosyer.ui.base.vm.viewModel
import kotlinx.coroutines.launch

@Composable
fun ApplicationScope.Tray(icon: Painter, resources: XmlResourceBundle) {
    val vm = viewModel<TrayViewModel>()
    val trayState = rememberTrayState()
    Tray(
        icon,
        trayState,
        tooltip = BuildConfig.NAME,
        menu = {
            Item(resources.getStringA("action_close"), onClick = ::exitApplication)
        }
    )

    LaunchedEffect(Unit) {
        launch {
            vm.updateFound.collect {
                trayState.sendNotification(
                    Notification(
                        resources.getStringA("new_update_title"),
                        resources.getString("new_update_message", it.version),
                        Notification.Type.Info
                    )
                )
            }
        }
    }
}
