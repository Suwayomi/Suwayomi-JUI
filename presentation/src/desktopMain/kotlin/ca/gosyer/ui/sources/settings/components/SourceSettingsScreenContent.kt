/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.sources.settings.components

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.Checkbox
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import ca.gosyer.i18n.MR
import ca.gosyer.presentation.build.BuildKonfig
import ca.gosyer.ui.base.WindowDialog
import ca.gosyer.ui.base.navigation.Toolbar
import ca.gosyer.ui.base.prefs.ChoiceDialog
import ca.gosyer.ui.base.prefs.MultiSelectDialog
import ca.gosyer.ui.base.prefs.PreferenceRow
import ca.gosyer.ui.sources.settings.model.SourceSettingsView
import ca.gosyer.ui.sources.settings.model.SourceSettingsView.CheckBox
import ca.gosyer.ui.sources.settings.model.SourceSettingsView.EditText
import ca.gosyer.ui.sources.settings.model.SourceSettingsView.List
import ca.gosyer.ui.sources.settings.model.SourceSettingsView.MultiSelect
import ca.gosyer.ui.sources.settings.model.SourceSettingsView.Switch
import ca.gosyer.ui.sources.settings.model.SourceSettingsView.TwoState
import ca.gosyer.uicore.resources.stringResource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.collections.List as KtList

@Composable
fun SourceSettingsScreenContent(
    settings: KtList<SourceSettingsView<*, *>>
) {
    Column {
        Toolbar(stringResource(MR.strings.location_settings))
        Box {
            val state = rememberLazyListState()
            LazyColumn(Modifier.fillMaxSize(), state) {
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
                        is MultiSelect -> {
                            MultiSelectPreference(it)
                        }
                    }
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
                state,
                onSelected = list::updateState,
                title = title
            )
        }
    )
}

@Composable
private fun MultiSelectPreference(multiSelect: MultiSelect) {
    val state by multiSelect.state.collectAsState()
    val title = remember(state) { multiSelect.title ?: multiSelect.summary ?: "No title" }
    val subtitle = remember(state) {
        if (multiSelect.title == null) {
            null
        } else {
            multiSelect.summary
        }
    }
    val dialogTitle = remember(state) { multiSelect.props.dialogTitle ?: multiSelect.title ?: multiSelect.summary ?: "No title" }
    PreferenceRow(
        title,
        subtitle = subtitle,
        onClick = {
            MultiSelectDialog(
                multiSelect.getOptions(),
                state,
                onFinished = multiSelect::updateState,
                title = dialogTitle
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
                editText.dialogTitle ?: BuildKonfig.NAME,
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
