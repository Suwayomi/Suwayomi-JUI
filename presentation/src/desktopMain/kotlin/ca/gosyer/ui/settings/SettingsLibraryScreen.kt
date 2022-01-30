/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.settings

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ca.gosyer.data.library.LibraryPreferences
import ca.gosyer.data.server.interactions.CategoryInteractionHandler
import ca.gosyer.i18n.MR
import ca.gosyer.ui.base.navigation.MenuController
import ca.gosyer.ui.base.navigation.Toolbar
import ca.gosyer.ui.base.prefs.PreferenceRow
import ca.gosyer.ui.base.prefs.SwitchPreference
import ca.gosyer.uicore.vm.ViewModel
import ca.gosyer.uicore.vm.viewModel
import ca.gosyer.ui.categories.openCategoriesMenu
import ca.gosyer.uicore.resources.stringResource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Inject

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
            _categories.value = categoryHandler.getCategories(true).size
        }
    }
}

@Composable
fun SettingsLibraryScreen(menuController: MenuController) {
    val vm = viewModel<SettingsLibraryViewModel>()

    Column {
        Toolbar(stringResource(MR.strings.settings_library_screen), menuController, true)
        Box {
            val state = rememberLazyListState()
            LazyColumn(Modifier.fillMaxSize(), state) {
                item {
                    SwitchPreference(
                        preference = vm.showAllCategory,
                        title = stringResource(MR.strings.show_all_category)
                    )
                }
                item {
                    PreferenceRow(
                        stringResource(MR.strings.location_categories),
                        onClick = { openCategoriesMenu(vm::refreshCategoryCount) },
                        subtitle = vm.categories.collectAsState().value.toString()
                    )
                }
            }
            VerticalScrollbar(
                rememberScrollbarAdapter(state),
                Modifier.align(Alignment.CenterEnd)
                    .fillMaxHeight()
                    .padding(horizontal = 4.dp, vertical = 8.dp)
            )
        }
    }
}
