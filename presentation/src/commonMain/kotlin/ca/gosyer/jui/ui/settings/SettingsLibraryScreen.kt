/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Divider
import androidx.compose.material.LocalContentColor
import androidx.compose.material.Scaffold
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ca.gosyer.jui.domain.category.interactor.GetCategories
import ca.gosyer.jui.domain.library.model.DisplayMode
import ca.gosyer.jui.domain.library.service.LibraryPreferences
import ca.gosyer.jui.i18n.MR
import ca.gosyer.jui.ui.base.dialog.getMaterialDialogProperties
import ca.gosyer.jui.ui.base.navigation.Toolbar
import ca.gosyer.jui.ui.base.prefs.ChoicePreference
import ca.gosyer.jui.ui.base.prefs.PreferenceRow
import ca.gosyer.jui.ui.categories.rememberCategoriesLauncher
import ca.gosyer.jui.ui.viewModel
import ca.gosyer.jui.uicore.components.VerticalScrollbar
import ca.gosyer.jui.uicore.components.rememberScrollbarAdapter
import ca.gosyer.jui.uicore.components.scrollbarPadding
import ca.gosyer.jui.uicore.prefs.PreferenceMutableStateFlow
import ca.gosyer.jui.uicore.resources.stringResource
import ca.gosyer.jui.uicore.vm.ContextWrapper
import ca.gosyer.jui.uicore.vm.ViewModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import cafe.adriel.voyager.core.screen.uniqueScreenKey
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.MaterialDialogState
import com.vanpra.composematerialdialogs.rememberMaterialDialogState
import com.vanpra.composematerialdialogs.title
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Inject
import kotlin.math.roundToInt

class SettingsLibraryScreen : Screen {
    override val key: ScreenKey = uniqueScreenKey

    @Composable
    override fun Content() {
        val vm = viewModel { settingsLibraryViewModel() }
        val categoriesLauncher = rememberCategoriesLauncher(vm::refreshCategoryCount)
        SettingsLibraryScreenContent(
            showAllCategory = vm.showAllCategory,
            displayMode = vm.displayMode,
            displayModeChoices = vm.getDisplayModeChoices(),
            gridColumns = vm.gridColumns,
            gridSize = vm.gridSize,
            categoriesSize = vm.categories.collectAsState().value,
            openCategoriesScreen = categoriesLauncher::open
        )
        categoriesLauncher.CategoriesWindow()
    }
}

class SettingsLibraryViewModel @Inject constructor(
    libraryPreferences: LibraryPreferences,
    private val getCategories: GetCategories,
    contextWrapper: ContextWrapper
) : ViewModel(contextWrapper) {

    val displayMode = libraryPreferences.displayMode().asStateFlow()
    val gridColumns = libraryPreferences.gridColumns().asStateFlow()
    val gridSize = libraryPreferences.gridSize().asStateFlow()

    val showAllCategory = libraryPreferences.showAllCategory().asStateFlow()
    private val _categories = MutableStateFlow(0)
    val categories = _categories.asStateFlow()

    init {
        refreshCategoryCount()
    }

    fun refreshCategoryCount() {
        scope.launch {
            _categories.value = getCategories.await(true)?.size ?: 0
        }
    }

    @Composable
    fun getDisplayModeChoices() = DisplayMode.values()
        .associateWith { stringResource(it.res) }
}

@Composable
fun SettingsLibraryScreenContent(
    displayMode: PreferenceMutableStateFlow<DisplayMode>,
    displayModeChoices: Map<DisplayMode, String>,
    gridColumns: PreferenceMutableStateFlow<Int>,
    gridSize: PreferenceMutableStateFlow<Int>,
    showAllCategory: PreferenceMutableStateFlow<Boolean>,
    categoriesSize: Int,
    openCategoriesScreen: () -> Unit
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
                    ChoicePreference(
                        preference = displayMode,
                        choices = displayModeChoices,
                        title = stringResource(MR.strings.display_mode)
                    )
                }
                item {
                    val displayModePref by displayMode.collectAsState()
                    GridPreference(
                        columnPreference = gridColumns,
                        sizePreference = gridSize,
                        title = stringResource(MR.strings.items_per_row),
                        enabled = displayModePref != DisplayMode.List
                    )
                }
                /*item {
                    SwitchPreference(
                        preference = showAllCategory,
                        title = stringResource(MR.strings.show_all_category)
                    )
                }*/
                item {
                    Divider()
                }
                item {
                    PreferenceRow(
                        stringResource(MR.strings.location_categories),
                        onClick = { openCategoriesScreen() },
                        subtitle = categoriesSize.toString()
                    )
                }
            }
            VerticalScrollbar(
                rememberScrollbarAdapter(state),
                Modifier.align(Alignment.CenterEnd)
                    .fillMaxHeight()
                    .scrollbarPadding()
            )
        }
    }
}

@Composable
private fun GridPreference(
    columnPreference: PreferenceMutableStateFlow<Int>,
    sizePreference: PreferenceMutableStateFlow<Int>,
    title: String,
    enabled: Boolean
) {
    val columnPrefValue by columnPreference.collectAsState()
    val sizePrefValue by sizePreference.collectAsState()
    val dialogState = rememberMaterialDialogState()
    PreferenceRow(
        title = title,
        subtitle = if (columnPrefValue < 1) {
            stringResource(MR.strings.items_per_row_sub_adaptive, sizePrefValue)
        } else {
            stringResource(MR.strings.items_per_row_sub, columnPrefValue)
        },
        onClick = {
            dialogState.show()
        },
        enabled = enabled
    )
    GridPrefDialog(
        state = dialogState,
        initialColumns = columnPrefValue,
        initialSize = sizePrefValue,
        title = title,
        onSelected = { columns, size ->
            columnPreference.value = columns
            sizePreference.value = size
        }
    )
}

@Composable
private fun GridPrefDialog(
    state: MaterialDialogState,
    initialColumns: Int,
    initialSize: Int,
    onCloseRequest: () -> Unit = {},
    onSelected: (columns: Int, size: Int) -> Unit,
    title: String
) {
    var columns by remember(initialColumns) { mutableStateOf(initialColumns.toFloat()) }
    var size by remember(initialSize) { mutableStateOf(initialSize.toFloat()) }
    MaterialDialog(
        state,
        buttons = {
            positiveButton(stringResource(MR.strings.action_ok)) {
                val sizeInt = size.roundToInt()
                val newSize = (10 - sizeInt % 10) + sizeInt
                onSelected(columns.roundToInt(), newSize)
            }
            negativeButton(stringResource(MR.strings.action_cancel))
        },
        properties = getMaterialDialogProperties(),
        onCloseRequest = {
            state.hide()
            onCloseRequest()
        }
    ) {
        title(title)
        Column(Modifier.padding(horizontal = 8.dp)) {
            Text(stringResource(MR.strings.grid_columns) + ":")
            Slider(
                value = columns,
                onValueChange = {
                    columns = it
                },
                modifier = Modifier.fillMaxWidth(),
                valueRange = 0F..10F,
                steps = 10 - 2
            )
            val columnsInt = columns.roundToInt()
            val adaptive = columnsInt < 1
            Text(
                if (adaptive) {
                    stringResource(MR.strings.adaptive)
                } else {
                    columnsInt.toString()
                },
                color = LocalContentColor.current.copy(alpha = ContentAlpha.disabled),
                fontSize = 12.sp
            )

            AnimatedVisibility(adaptive) {
                Column {
                    Spacer(Modifier.height(16.dp))
                    Text(stringResource(MR.strings.grid_size) + ":")
                    Slider(
                        value = size,
                        onValueChange = {
                            size = it
                        },
                        modifier = Modifier.fillMaxWidth(),
                        valueRange = 90F..300F,
                        steps = 21 - 2
                    )
                    val sizeInt = size.roundToInt()
                    val newSize = (10 - sizeInt % 10) + sizeInt
                    Text(
                        newSize.toString(),
                        color = LocalContentColor.current.copy(alpha = ContentAlpha.disabled),
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
