/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.sources.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import ca.gosyer.ui.sources.settings.components.SourceSettingsScreenContent
import ca.gosyer.uicore.vm.viewModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import cafe.adriel.voyager.core.screen.uniqueScreenKey

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
