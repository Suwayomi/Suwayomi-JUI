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
import ca.gosyer.ui.base.vm.ViewModel
import ca.gosyer.ui.base.vm.viewModel
import ca.gosyer.ui.main.Route
import com.github.zsoltk.compose.router.BackStack
import javax.inject.Inject

class SettingsServerViewModel @Inject constructor(
    private val serverPreferences: ServerPreferences
): ViewModel() {
    val host = serverPreferences.host().asStateIn(scope)
    val serverUrl = serverPreferences.server().asStateIn(scope)
}

@Composable
fun SettingsServerScreen(navController: BackStack<Route>) {
    val vm = viewModel<SettingsServerViewModel>()
    Column {
        Toolbar("Server Settings", navController, true)
        SwitchPreference(preference = vm.host, title = "Host server inside TachideskJUI")
        LazyColumn {
            item {
                EditTextPreference(vm.serverUrl, "Server Url", subtitle = vm.serverUrl.collectAsState().value)
            }
        }
    }
}
