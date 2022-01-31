/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.extensions

import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.rememberWindowState
import ca.gosyer.presentation.build.BuildKonfig
import ca.gosyer.ui.AppComponent
import ca.gosyer.ui.extensions.components.ExtensionsScreenContent
import ca.gosyer.ui.util.compose.ThemedWindow
import ca.gosyer.ui.util.lang.launchApplication
import ca.gosyer.uicore.vm.viewModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import cafe.adriel.voyager.core.screen.uniqueScreenKey
import cafe.adriel.voyager.navigator.Navigator
import kotlinx.coroutines.DelicateCoroutinesApi

@OptIn(DelicateCoroutinesApi::class)
fun openExtensionsMenu() {
    launchApplication {
        CompositionLocalProvider(*remember { AppComponent.getInstance().uiComponent.getHooks() }) {
            val state = rememberWindowState(size = DpSize(550.dp, 700.dp))
            ThemedWindow(::exitApplication, state, title = BuildKonfig.NAME) {
                Surface {
                    Navigator(remember { ExtensionsScreen() })
                }
            }
        }
    }
}

class ExtensionsScreen : Screen {

    override val key: ScreenKey = uniqueScreenKey

    @Composable
    override fun Content() {
        val vm = viewModel<ExtensionsScreenViewModel>()

        ExtensionsScreenContent(
            extensions = vm.extensions.collectAsState().value,
            isLoading = vm.isLoading.collectAsState().value,
            query = vm.searchQuery.collectAsState().value,
            setQuery = vm::search,
            enabledLangs = vm.enabledLangs,
            getSourceLanguages = vm::getSourceLanguages,
            setEnabledLanguages = vm::setEnabledLanguages,
            installExtension = vm::install,
            updateExtension = vm::update,
            uninstallExtension = vm::uninstall
        )
    }
}
