/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.sources.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Checkbox
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import ca.gosyer.BuildConfig
import ca.gosyer.ui.base.WindowDialog
import ca.gosyer.ui.base.components.Toolbar
import ca.gosyer.ui.base.prefs.ChoiceDialog
import ca.gosyer.ui.base.prefs.PreferenceRow
import ca.gosyer.ui.base.resources.stringResource
import ca.gosyer.ui.base.vm.viewModel
import ca.gosyer.ui.main.Route
import ca.gosyer.ui.sources.settings.model.SourceSettingsView.CheckBox
import ca.gosyer.ui.sources.settings.model.SourceSettingsView.EditText
import ca.gosyer.ui.sources.settings.model.SourceSettingsView.List
import ca.gosyer.ui.sources.settings.model.SourceSettingsView.Switch
import ca.gosyer.ui.sources.settings.model.SourceSettingsView.TwoState
import ca.gosyer.util.compose.ThemedWindow
import ca.gosyer.util.lang.launchApplication
import com.github.zsoltk.compose.router.BackStack
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow

@OptIn(DelicateCoroutinesApi::class)
fun openSourceSettingsMenu(sourceId: Long) {
    launchApplication {
        ThemedWindow(::exitApplication, title = BuildConfig.NAME) {
            SourceSettingsMenu(sourceId)
        }
    }
}

@Composable
fun SourceSettingsMenu(sourceId: Long, backStack: BackStack<Route>? = null) {
    val vm = viewModel<SourceSettingsViewModel> {
        SourceSettingsViewModel.Params(sourceId)
    }
    val settings by vm.sourceSettings.collectAsState()

    Column {
        Toolbar(stringResource("location_settings"), backStack, backStack != null)
        LazyColumn {
            items(settings, { it.props.hashCode() }) {
                when (it) {
                    is CheckBox, is Switch -> {
                        TwoStatePreference(it as TwoState, it is CheckBox)
                    }
                    is List -> {
                        ListPreference(it)
                    }
                    is EditText -> {
                        EditTextPreference(it)
                    }
                    else -> Unit
                }
            }
        }
    }
}

@Composable
private fun TwoStatePreference(twoState: TwoState, checkbox: Boolean) {
    val state by twoState.state.collectAsState()
    val title = remember(state) { twoState.title ?: twoState.summary ?: "No title" }
    val subtitle = remember(state) {
        if (twoState.title == null) {
            null
        } else {
            twoState.summary
        }
    }
    PreferenceRow(
        title,
        subtitle = subtitle,
        onClick = { twoState.updateState(!state) },
        action = {
            if (checkbox) {
                Checkbox(checked = state, onCheckedChange = null)
            } else {
                Switch(checked = state, onCheckedChange = null)
            }
        }
    )
}

@Composable
private fun ListPreference(list: List) {
    val state by list.state.collectAsState()
    val title = remember(state) { list.title ?: list.summary ?: "No title" }
    val subtitle = remember(state) {
        if (list.title == null) {
            null
        } else {
            list.summary
        }
    }
    PreferenceRow(
        title,
        subtitle = subtitle,
        onClick = {
            ChoiceDialog(
                list.getOptions(),
                state.first,
                onSelected = list::setValue,
                title = "Select choice"
            )
        }
    )
}

@Composable
private fun EditTextPreference(editText: EditText) {
    val state by editText.state.collectAsState()
    val title = remember(state) { editText.title ?: editText.summary ?: "No title" }
    val subtitle = remember(state) {
        if (editText.title == null) {
            null
        } else {
            editText.summary
        }
    }
    PreferenceRow(
        title,
        subtitle = subtitle,
        onClick = {
            val editTextFlow = MutableStateFlow(TextFieldValue(state))
            WindowDialog(
                editText.dialogTitle ?: BuildConfig.NAME,
                onPositiveButton = {
                    editText.updateState(editTextFlow.value.text)
                }
            ) {
                if (editText.dialogMessage != null) {
                    Text(editText.dialogMessage)
                    Spacer(Modifier.height(8.dp))
                }

                val text by editTextFlow.collectAsState()
                OutlinedTextField(
                    text,
                    onValueChange = {
                        editTextFlow.value = it
                    }
                )
            }
        }
    )
}
