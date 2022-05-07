/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import ca.gosyer.jui.data.update.UpdatePreferences
import ca.gosyer.jui.i18n.MR
import ca.gosyer.jui.ui.base.navigation.Toolbar
import ca.gosyer.jui.ui.base.prefs.SwitchPreference
import ca.gosyer.jui.uicore.components.VerticalScrollbar
import ca.gosyer.jui.uicore.components.rememberScrollbarAdapter
import ca.gosyer.jui.uicore.components.scrollbarPadding
import ca.gosyer.jui.uicore.prefs.PreferenceMutableStateFlow
import ca.gosyer.jui.uicore.resources.stringResource
import ca.gosyer.jui.uicore.vm.ContextWrapper
import ca.gosyer.jui.uicore.vm.ViewModel
import ca.gosyer.jui.uicore.vm.viewModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import cafe.adriel.voyager.core.screen.uniqueScreenKey
import me.tatarka.inject.annotations.Inject

class SettingsAdvancedScreen : Screen {
    override val key: ScreenKey = uniqueScreenKey

    @Composable
    override fun Content() {
        val vm = viewModel<SettingsAdvancedViewModel>()
        SettingsAdvancedScreenContent(
            updatesEnabled = vm.updatesEnabled
        )
    }
}

class SettingsAdvancedViewModel @Inject constructor(
    updatePreferences: UpdatePreferences,
    contextWrapper: ContextWrapper
) : ViewModel(contextWrapper) {
    val updatesEnabled = updatePreferences.enabled().asStateFlow()
}

@Composable
fun SettingsAdvancedScreenContent(
    updatesEnabled: PreferenceMutableStateFlow<Boolean>
) {
    Scaffold(
        topBar = {
            Toolbar(stringResource(MR.strings.settings_advanced_screen))
        }
    ) {
        Box(Modifier.padding(it)) {
            val state = rememberLazyListState()
            LazyColumn(Modifier.fillMaxSize(), state) {
                item {
                    SwitchPreference(preference = updatesEnabled, title = stringResource(MR.strings.update_checker))
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
