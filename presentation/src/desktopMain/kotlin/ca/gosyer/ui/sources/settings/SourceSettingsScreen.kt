/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.sources.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import ca.gosyer.presentation.build.BuildKonfig
import ca.gosyer.ui.sources.settings.components.SourceSettingsScreenContent
import ca.gosyer.ui.util.compose.ThemedWindow
import ca.gosyer.ui.util.lang.launchApplication
import ca.gosyer.uicore.vm.viewModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import cafe.adriel.voyager.core.screen.uniqueScreenKey
import cafe.adriel.voyager.navigator.Navigator
import kotlinx.coroutines.DelicateCoroutinesApi

@OptIn(DelicateCoroutinesApi::class)
fun openSourceSettingsMenu(sourceId: Long) {
    launchApplication {
        ThemedWindow(::exitApplication, title = BuildKonfig.NAME) {
            Navigator(remember { SourceSettingsScreen(sourceId) })
        }
    }
}

class SourceSettingsScreen(private val sourceId: Long) : Screen {

    override val key: ScreenKey = uniqueScreenKey

    @Composable
    override fun Content() {
        val vm = viewModel {
            instantiate<SourceSettingsScreenViewModel>(SourceSettingsScreenViewModel.Params(sourceId))
        }
        SourceSettingsScreenContent(
            settings = vm.sourceSettings.collectAsState().value
        )
    }
}
