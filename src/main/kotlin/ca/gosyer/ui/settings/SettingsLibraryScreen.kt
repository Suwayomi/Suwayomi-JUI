/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import ca.gosyer.data.library.LibraryPreferences
import ca.gosyer.ui.base.components.Toolbar
import ca.gosyer.ui.base.prefs.SwitchPreference
import ca.gosyer.ui.base.vm.ViewModel
import ca.gosyer.ui.base.vm.viewModel
import ca.gosyer.ui.main.Route
import com.github.zsoltk.compose.router.BackStack
import javax.inject.Inject

class SettingsLibraryViewModel @Inject constructor(
    libraryPreferences: LibraryPreferences
) : ViewModel() {

    val showAllCategory = libraryPreferences.showAllCategory().asStateFlow()
}

@Composable
fun SettingsLibraryScreen(navController: BackStack<Route>) {
    val vm = viewModel<SettingsLibraryViewModel>()

    Column {
        Toolbar("Library Settings", navController, true)
        LazyColumn {
            item {
                SwitchPreference(preference = vm.showAllCategory, title = "Show all category")
            }
        }
    }
}
