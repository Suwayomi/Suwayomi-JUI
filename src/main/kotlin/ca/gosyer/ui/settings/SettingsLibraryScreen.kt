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
import ca.gosyer.data.library.LibraryPreferences
import ca.gosyer.data.server.interactions.CategoryInteractionHandler
import ca.gosyer.ui.base.components.Toolbar
import ca.gosyer.ui.base.prefs.PreferenceRow
import ca.gosyer.ui.base.prefs.SwitchPreference
import ca.gosyer.ui.base.resources.stringResource
import ca.gosyer.ui.base.vm.ViewModel
import ca.gosyer.ui.base.vm.viewModel
import ca.gosyer.ui.categories.openCategoriesMenu
import ca.gosyer.ui.main.Route
import com.github.zsoltk.compose.router.BackStack
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class SettingsLibraryViewModel @Inject constructor(
    libraryPreferences: LibraryPreferences,
    private val categoryHandler: CategoryInteractionHandler
) : ViewModel() {

    val showAllCategory = libraryPreferences.showAllCategory().asStateFlow()
    private val _categories = MutableStateFlow(0)
    val categories = _categories.asStateFlow()

    init {
        refreshCategoryCount()
    }

    fun refreshCategoryCount() {
        scope.launch {
            _categories.value = categoryHandler.getCategories().size
        }
    }
}

@Composable
fun SettingsLibraryScreen(navController: BackStack<Route>) {
    val vm = viewModel<SettingsLibraryViewModel>()

    Column {
        Toolbar(stringResource("settings_library_screen"), navController, true)
        LazyColumn {
            item {
                SwitchPreference(preference = vm.showAllCategory, title = stringResource("show_all_category"))
            }
            item {
                PreferenceRow(
                    stringResource("location_categories"),
                    onClick = { openCategoriesMenu(vm::refreshCategoryCount) },
                    subtitle = vm.categories.collectAsState().value.toString()
                )
            }
        }
    }
}
