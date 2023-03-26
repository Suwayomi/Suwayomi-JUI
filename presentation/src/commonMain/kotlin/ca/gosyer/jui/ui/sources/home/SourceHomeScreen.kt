/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.sources.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import ca.gosyer.jui.ui.base.screen.BaseScreen
import ca.gosyer.jui.ui.sources.browse.SourceScreen
import ca.gosyer.jui.ui.sources.components.LocalSourcesNavigator
import ca.gosyer.jui.ui.sources.globalsearch.GlobalSearchScreen
import ca.gosyer.jui.ui.sources.home.components.SourceHomeScreenContent
import ca.gosyer.jui.ui.stateViewModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow

class SourceHomeScreen : BaseScreen() {

    @Composable
    override fun Content() {
        val vm = stateViewModel { sourceHomeViewModel(it) }
        val sourcesNavigator = LocalSourcesNavigator.current
        val navigator = LocalNavigator.currentOrThrow
        SourceHomeScreenContent(
            onAddSource = if (sourcesNavigator != null) {
                { sourcesNavigator.open(it) }
            } else {
                { navigator push SourceScreen(it) }
            },
            isLoading = vm.isLoading.collectAsState().value,
            sources = vm.sources.collectAsState().value,
            languages = vm.languages.collectAsState().value,
            sourceLanguages = vm.sourceLanguages.collectAsState().value,
            setEnabledLanguages = vm::setEnabledLanguages,
            query = vm.query.collectAsState().value,
            setQuery = vm::setQuery,
            submitSearch = if (sourcesNavigator != null) {
                { sourcesNavigator.search(it) }
            } else {
                { navigator push GlobalSearchScreen(it) }
            },
        )
    }
}
