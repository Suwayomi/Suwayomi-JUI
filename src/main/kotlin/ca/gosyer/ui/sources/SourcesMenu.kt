/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.sources

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import ca.gosyer.data.models.Source
import ca.gosyer.ui.base.vm.viewModel
import ca.gosyer.ui.sources.components.SourceHomeScreen
import ca.gosyer.ui.sources.components.SourceScreen
import ca.gosyer.ui.sources.components.SourceTopBar
import ca.gosyer.util.compose.ThemedWindow

fun openSourcesMenu() {
    ThemedWindow(title = "TachideskJUI - Sources") {
        SourcesMenu()
    }
}

@Composable
fun SourcesMenu() {
    val vm = viewModel<SourcesMenuViewModel>()
    val isLoading by vm.isLoading.collectAsState()
    val sources by vm.sources.collectAsState()
    val sourceTabs by vm.sourceTabs.collectAsState()
    val selectedSourceTab by vm.selectedSourceTab.collectAsState()
    val serverUrl by vm.serverUrl.collectAsState()

    Column(Modifier.fillMaxSize().background(MaterialTheme.colors.background)) {
        SourceTopBar(
            openHome = {
                vm.selectTab(null)
            },
            sources = sourceTabs,
            tabSelected = selectedSourceTab,
            onTabSelected = vm::selectTab,
            onTabClosed = vm::closeTab
        )

        val selectedSource: Source? = selectedSourceTab
        if (selectedSource != null) {
            SourceScreen(selectedSource)
        } else {
            SourceHomeScreen(isLoading, sources, serverUrl, vm::addTab)
        }
    }
}
