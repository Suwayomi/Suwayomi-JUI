/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.sources

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ca.gosyer.data.models.Source
import ca.gosyer.ui.base.components.KtorImage
import ca.gosyer.ui.base.components.Toolbar
import ca.gosyer.ui.base.vm.viewModel
import ca.gosyer.ui.manga.openMangaMenu
import ca.gosyer.ui.sources.components.SourceHomeScreen
import ca.gosyer.ui.sources.components.SourceScreen
import ca.gosyer.util.compose.ThemedWindow
import com.github.zsoltk.compose.savedinstancestate.BundleScope

fun openSourcesMenu() {
    ThemedWindow(title = "TachideskJUI - Sources") {
        SourcesMenu {
            openMangaMenu(it)
        }
    }
}

@Composable
fun SourcesMenu(onMangaClick: (Long) -> Unit) {
    val vm = viewModel<SourcesMenuViewModel>()
    val isLoading by vm.isLoading.collectAsState()
    val sources by vm.sources.collectAsState()
    val sourceTabs by vm.sourceTabs.collectAsState()
    val selectedSourceTab by vm.selectedSourceTab.collectAsState()
    val serverUrl by vm.serverUrl.collectAsState()

    Surface {
        Column {
            Toolbar(selectedSourceTab?.name ?: "Sources", closable = false)
            Row {
                LazyColumn(Modifier.fillMaxHeight().width(64.dp)) {
                    items(sourceTabs) { source ->
                        Card(
                            Modifier
                                .clickable {
                                    vm.selectTab(source)
                                }
                                .requiredHeight(64.dp)
                                .requiredWidth(64.dp),
                        ) {
                            if (source != null) {
                                KtorImage(source.iconUrl(serverUrl),)
                            } else {
                                Icon(Icons.Default.Home, "Home")
                            }
                        }
                    }
                }

                val selectedSource: Source? = selectedSourceTab
                BundleScope(selectedSource?.name ?: "home") {
                    if (selectedSource != null) {
                        SourceScreen(selectedSource, onMangaClick)
                    } else {
                        SourceHomeScreen(isLoading, sources, serverUrl, vm::addTab)
                    }
                }
            }
        }
    }
}
