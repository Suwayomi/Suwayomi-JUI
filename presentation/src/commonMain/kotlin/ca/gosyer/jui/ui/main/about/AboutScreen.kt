/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.main.about

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalUriHandler
import ca.gosyer.jui.ui.main.about.components.AboutContent
import ca.gosyer.jui.ui.main.about.licenses.LicensesScreen
import ca.gosyer.jui.ui.viewModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import cafe.adriel.voyager.core.screen.uniqueScreenKey
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.launch

class AboutScreen : Screen {

    override val key: ScreenKey = uniqueScreenKey

    @Composable
    override fun Content() {
        val vm = viewModel { aboutViewModel() }
        val uriHandler = LocalUriHandler.current
        val navigator = LocalNavigator.currentOrThrow
        LaunchedEffect(vm) {
            launch {
                vm.updates.collect {
                    uriHandler.openUri(it.release.releaseLink)
                }
            }
        }
        AboutContent(
            about = vm.aboutHolder.collectAsState().value,
            formattedBuildTime = vm.formattedBuildTime.collectAsState().value,
            checkForUpdates = vm::checkForUpdates,
            openSourceLicenses = {
                navigator push LicensesScreen()
            },
        )
    }
}
