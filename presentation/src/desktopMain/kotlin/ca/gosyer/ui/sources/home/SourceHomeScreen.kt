/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.sources.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import ca.gosyer.ui.sources.components.LocalSourcesNavigator
import ca.gosyer.ui.sources.home.components.SourceHomeScreenContent
import ca.gosyer.uicore.vm.viewModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import cafe.adriel.voyager.core.screen.uniqueScreenKey

class SourceHomeScreen : Screen {

    override val key: ScreenKey = uniqueScreenKey

    @Composable
    override fun Content() {
        val vm = viewModel<SourceHomeScreenViewModel>()
        val sourcesNavigator = LocalSourcesNavigator.current
        SourceHomeScreenContent(
            onAddSource = sourcesNavigator::select,
            isLoading = vm.isLoading.collectAsState().value,
            sources = vm.sources.collectAsState().value,
            languages = vm.languages,
            getSourceLanguages = vm::getSourceLanguages,
            setEnabledLanguages = vm::setEnabledLanguages
        )
    }
}
