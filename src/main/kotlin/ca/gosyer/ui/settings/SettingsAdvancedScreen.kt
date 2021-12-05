/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import ca.gosyer.data.update.UpdatePreferences
import ca.gosyer.ui.base.components.MenuController
import ca.gosyer.ui.base.components.Toolbar
import ca.gosyer.ui.base.prefs.SwitchPreference
import ca.gosyer.ui.base.resources.stringResource
import ca.gosyer.ui.base.vm.ViewModel
import ca.gosyer.ui.base.vm.viewModel
import javax.inject.Inject

class SettingsAdvancedViewModel @Inject constructor(
    updatePreferences: UpdatePreferences,
) : ViewModel() {
    val updatesEnabled = updatePreferences.enabled().asStateFlow()
}

@Composable
fun SettingsAdvancedScreen(menuController: MenuController) {
    val vm = viewModel<SettingsAdvancedViewModel>()
    Column {
        Toolbar(stringResource("settings_advanced_screen"), menuController, true)
        LazyColumn {
            item {
                SwitchPreference(preference = vm.updatesEnabled, title = stringResource("update_checker"))
            }
        }
    }
}
