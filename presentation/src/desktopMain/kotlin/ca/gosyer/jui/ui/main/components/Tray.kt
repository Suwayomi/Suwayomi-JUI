/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.main.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.Notification
import androidx.compose.ui.window.Tray
import androidx.compose.ui.window.rememberTrayState
import ca.gosyer.jui.i18n.MR
import ca.gosyer.jui.presentation.build.BuildKonfig
import ca.gosyer.jui.ui.base.LocalViewModels
import ca.gosyer.jui.ui.base.model.StableHolder
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun ApplicationScope.Tray(icon: StableHolder<Painter>) {
    val viewModels = LocalViewModels.current
    val vm = remember { viewModels.trayViewModel() }
    val trayState = rememberTrayState()
    Tray(
        icon.item,
        trayState,
        tooltip = BuildKonfig.NAME,
        menu = {
            Item(MR.strings.action_close.localized(), onClick = ::exitApplication)
        },
    )

    LaunchedEffect(Unit) {
        launch {
            vm.updateFound.collect {
                trayState.sendNotification(
                    Notification(
                        MR.strings.new_update_title.localized(),
                        MR.strings.new_update_message.localized(Locale.getDefault(), it.release.version),
                        Notification.Type.Info,
                    ),
                )
            }
        }
    }
}
