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
import ca.gosyer.jui.uicore.vm.viewModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import cafe.adriel.voyager.core.screen.uniqueScreenKey
import kotlinx.coroutines.launch

class AboutScreen : Screen {

    override val key: ScreenKey = uniqueScreenKey

    @Composable
    override fun Content() {
        val vm = viewModel<AboutViewModel>()
        val uriHandler = LocalUriHandler.current
        LaunchedEffect(vm) {
            launch {
                vm.updates.collect {
                    uriHandler.openUri(it.release.releaseLink)
                }
            }
        }
        AboutContent(
            about = vm.about.collectAsState().value,
            formattedBuildTime = vm.formattedBuildTime.collectAsState().value,
            checkForUpdates = vm::checkForUpdates
        )
    }
}
