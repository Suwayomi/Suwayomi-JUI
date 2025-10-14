/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.extensions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import ca.gosyer.jui.ui.extensions.components.ExtensionsScreenContent
import ca.gosyer.jui.ui.viewModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import cafe.adriel.voyager.core.screen.uniqueScreenKey

class ExtensionsScreen : Screen {
    override val key: ScreenKey = uniqueScreenKey

    @Composable
    override fun Content() {
        val vm = viewModel { extensionsViewModel() }
        val state by vm.state.collectAsState()

        ExtensionsScreenContent(
            extensions = vm.extensions.collectAsState().value,
            isLoading = vm.isLoading.collectAsState().value,
            query = state.searchQuery,
            setQuery = vm::setQuery,
            enabledLangs = vm.enabledLangs.collectAsState().value,
            availableLangs = vm.availableLangs.collectAsState().value,
            setEnabledLanguages = vm::setEnabledLanguages,
            installExtensionFile = vm::install,
            installExtension = vm::install,
            updateExtension = vm::update,
            uninstallExtension = vm::uninstall,
        )
    }
}
