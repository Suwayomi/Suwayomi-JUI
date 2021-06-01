/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import ca.gosyer.data.server.ServerPreferences
import ca.gosyer.ui.base.components.Toolbar
import ca.gosyer.ui.base.prefs.EditTextPreference
import ca.gosyer.ui.base.prefs.SwitchPreference
import ca.gosyer.ui.base.prefs.asStateIn
import ca.gosyer.ui.base.prefs.asStringStateIn
import ca.gosyer.ui.base.vm.ViewModel
import ca.gosyer.ui.base.vm.viewModel
import ca.gosyer.ui.main.Route
import com.github.zsoltk.compose.router.BackStack
import javax.inject.Inject

class SettingsServerViewModel @Inject constructor(
    private val serverPreferences: ServerPreferences
) : ViewModel() {
    val host = serverPreferences.host().asStateIn(scope)
    val server = serverPreferences.server().asStateIn(scope)
    val port = serverPreferences.port().asStringStateIn(scope)
}

@Composable
fun SettingsServerScreen(navController: BackStack<Route>) {
    val vm = viewModel<SettingsServerViewModel>()
    Column {
        Toolbar("Server Settings", navController, true)
        SwitchPreference(preference = vm.host, title = "Host server inside TachideskJUI")
        LazyColumn {
            item {
                EditTextPreference(vm.server, "Server Url", subtitle = vm.server.collectAsState().value)
            }
            item {
                EditTextPreference(vm.port, "Server Url", subtitle = vm.port.collectAsState().value)
            }
        }
    }
}
