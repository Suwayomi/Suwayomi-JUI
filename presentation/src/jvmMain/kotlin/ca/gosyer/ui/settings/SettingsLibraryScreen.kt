/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.settings

import ca.gosyer.ui.base.components.VerticalScrollbar
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import ca.gosyer.ui.base.components.rememberScrollbarAdapter
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ca.gosyer.data.library.LibraryPreferences
import ca.gosyer.data.server.interactions.CategoryInteractionHandler
import ca.gosyer.i18n.MR
import ca.gosyer.ui.base.navigation.Toolbar
import ca.gosyer.ui.base.prefs.PreferenceRow
import ca.gosyer.ui.base.prefs.SwitchPreference
import ca.gosyer.ui.categories.openCategoriesMenu
import ca.gosyer.uicore.prefs.PreferenceMutableStateFlow
import ca.gosyer.uicore.resources.stringResource
import ca.gosyer.uicore.vm.ContextWrapper
import ca.gosyer.uicore.vm.ViewModel
import ca.gosyer.uicore.vm.viewModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import cafe.adriel.voyager.core.screen.uniqueScreenKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Inject

class SettingsLibraryScreen : Screen {
    override val key: ScreenKey = uniqueScreenKey

    @Composable
    override fun Content() {
        val vm = viewModel<SettingsLibraryViewModel>()
        SettingsLibraryScreenContent(
            showAllCategory = vm.showAllCategory,
            refreshCategoryCount = vm::refreshCategoryCount,
            categoriesSize = vm.categories.collectAsState().value
        )
    }
}

class SettingsLibraryViewModel @Inject constructor(
    libraryPreferences: LibraryPreferences,
    private val categoryHandler: CategoryInteractionHandler,
    contextWrapper: ContextWrapper
) : ViewModel(contextWrapper) {

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
fun SettingsLibraryScreenContent(
    showAllCategory: PreferenceMutableStateFlow<Boolean>,
    refreshCategoryCount: () -> Unit,
    categoriesSize: Int
) {
    Scaffold(
        topBar = {
            Toolbar(stringResource(MR.strings.settings_library_screen))
        }
    ) {
        Box(Modifier.padding(it)) {
            val state = rememberLazyListState()
            LazyColumn(Modifier.fillMaxSize(), state) {
                item {
                    SwitchPreference(
                        preference = showAllCategory,
                        title = stringResource(MR.strings.show_all_category)
                    )
                }
                item {
                    PreferenceRow(
                        stringResource(MR.strings.location_categories),
                        onClick = { openCategoriesMenu(refreshCategoryCount) },
                        subtitle = categoriesSize.toString()
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
