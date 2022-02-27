/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.extensions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import ca.gosyer.ui.extensions.components.ExtensionsScreenContent
import ca.gosyer.uicore.vm.viewModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import cafe.adriel.voyager.core.screen.uniqueScreenKey

class ExtensionsScreen : Screen {

    override val key: ScreenKey = uniqueScreenKey

    @Composable
    override fun Content() {
        val vm = viewModel<ExtensionsScreenViewModel>()

        ExtensionsScreenContent(
            extensions = vm.extensions.collectAsState().value,
            isLoading = vm.isLoading.collectAsState().value,
            query = vm.searchQuery.collectAsState().value,
            setQuery = vm::setQuery,
            enabledLangs = vm.enabledLangs.collectAsState().value,
            availableLangs = vm.availableLangs.collectAsState().value,
            setEnabledLanguages = vm::setEnabledLanguages,
            installExtension = vm::install,
            updateExtension = vm::update,
            uninstallExtension = vm::uninstall
        )
    }
}
