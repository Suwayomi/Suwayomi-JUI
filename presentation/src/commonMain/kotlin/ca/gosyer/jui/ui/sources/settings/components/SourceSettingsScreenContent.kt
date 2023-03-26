/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.sources.settings.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Checkbox
import androidx.compose.material.Scaffold
import androidx.compose.material.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import ca.gosyer.jui.i18n.MR
import ca.gosyer.jui.presentation.build.BuildKonfig
import ca.gosyer.jui.ui.base.dialog.getMaterialDialogProperties
import ca.gosyer.jui.ui.base.navigation.Toolbar
import ca.gosyer.jui.ui.base.prefs.ChoiceDialog
import ca.gosyer.jui.ui.base.prefs.MultiSelectDialog
import ca.gosyer.jui.ui.base.prefs.PreferenceRow
import ca.gosyer.jui.ui.main.components.bottomNav
import ca.gosyer.jui.ui.sources.settings.model.SourceSettingsView
import ca.gosyer.jui.ui.sources.settings.model.SourceSettingsView.CheckBox
import ca.gosyer.jui.ui.sources.settings.model.SourceSettingsView.EditText
import ca.gosyer.jui.ui.sources.settings.model.SourceSettingsView.List
import ca.gosyer.jui.ui.sources.settings.model.SourceSettingsView.MultiSelect
import ca.gosyer.jui.ui.sources.settings.model.SourceSettingsView.Switch
import ca.gosyer.jui.ui.sources.settings.model.SourceSettingsView.TwoState
import ca.gosyer.jui.uicore.components.VerticalScrollbar
import ca.gosyer.jui.uicore.components.keyboardHandler
import ca.gosyer.jui.uicore.components.rememberScrollbarAdapter
import ca.gosyer.jui.uicore.components.scrollbarPadding
import ca.gosyer.jui.uicore.insets.navigationBars
import ca.gosyer.jui.uicore.insets.statusBars
import ca.gosyer.jui.uicore.resources.stringResource
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.TextFieldStyle
import com.vanpra.composematerialdialogs.input
import com.vanpra.composematerialdialogs.message
import com.vanpra.composematerialdialogs.rememberMaterialDialogState
import com.vanpra.composematerialdialogs.title
import kotlinx.collections.immutable.ImmutableList

@Composable
fun SourceSettingsScreenContent(
    settings: ImmutableList<SourceSettingsView<*, *>>,
) {
    Scaffold(
        modifier = Modifier.windowInsetsPadding(
            WindowInsets.statusBars.add(
                WindowInsets.navigationBars.only(WindowInsetsSides.Horizontal),
            ),
        ),
        topBar = {
            Toolbar(stringResource(MR.strings.location_settings))
        },
    ) { padding ->
        Box(Modifier.padding(padding)) {
            val state = rememberLazyListState()
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = state,
                contentPadding = WindowInsets.bottomNav.add(
                    WindowInsets.navigationBars.only(
                        WindowInsetsSides.Bottom,
                    ),
                ).asPaddingValues(),
            ) {
                items(settings, { it.props.hashCode() }) {
                    @Suppress("UNCHECKED_CAST")
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
                    .scrollbarPadding()
                    .windowInsetsPadding(
                        WindowInsets.bottomNav.add(
                            WindowInsets.navigationBars.only(
                                WindowInsetsSides.Bottom,
                            ),
                        ),
                    ),
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
        },
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
    val dialogState = rememberMaterialDialogState()
    PreferenceRow(
        title,
        subtitle = subtitle,
        onClick = {
            dialogState.show()
        },
    )
    ChoiceDialog(
        dialogState,
        list.getOptions(),
        state,
        onSelected = list::updateState,
        title = title,
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
    val dialogState = rememberMaterialDialogState()
    PreferenceRow(
        title,
        subtitle = subtitle,
        onClick = {
            dialogState.show()
        },
    )
    MultiSelectDialog(
        dialogState,
        multiSelect.getOptions(),
        state,
        onFinished = multiSelect::updateState,
        title = dialogTitle,
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
    val dialogState = rememberMaterialDialogState()
    PreferenceRow(
        title,
        subtitle = subtitle,
        onClick = dialogState::show,
    )
    MaterialDialog(
        dialogState,
        buttons = {
            positiveButton(stringResource(MR.strings.action_ok))
            negativeButton(stringResource(MR.strings.action_cancel))
        },
        properties = getMaterialDialogProperties(),
    ) {
        title(editText.dialogTitle ?: BuildKonfig.NAME)
        if (editText.dialogMessage != null) {
            message(editText.dialogMessage)
        }
        input(
            label = "",
            textFieldStyle = TextFieldStyle.Outlined,
            onInput = { editText.updateState(it) },
            modifier = Modifier.keyboardHandler(),
        )
    }
}
