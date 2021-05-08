/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.extensions

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.Button
import androidx.compose.material.ContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ca.gosyer.data.models.Extension
import ca.gosyer.ui.base.components.KtorImage
import ca.gosyer.ui.base.components.LoadingScreen
import ca.gosyer.ui.base.components.Toolbar
import ca.gosyer.ui.base.vm.viewModel
import ca.gosyer.util.compose.ThemedWindow

fun openExtensionsMenu() {
    ThemedWindow(title = "TachideskJUI - Extensions", size = IntSize(550, 700)) {
        ExtensionsMenu()
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ExtensionsMenu() {
    val vm = viewModel<ExtensionsMenuViewModel>()
    val extensions by vm.extensions.collectAsState()
    val isLoading by vm.isLoading.collectAsState()
    val serverUrl by vm.serverUrl.collectAsState()

    Box(Modifier.fillMaxSize().background(MaterialTheme.colors.background)) {
        if (isLoading) {
            LoadingScreen(isLoading)
        } else {
            val state = rememberLazyListState()
            val itemCount = extensions.size

            Box(Modifier.fillMaxSize()) {
                LazyColumn(Modifier.fillMaxSize().padding(end = 12.dp), state) {
                    item {
                        Toolbar(
                            "Extensions",
                            closable = false,
                            search = {
                                vm.search(it)
                            }
                        )
                    }
                    items(extensions) { extension ->
                        ExtensionItem(
                            extension,
                            serverUrl,
                            onInstallClicked = {
                                vm.install(it)
                            },
                            onUninstallClicked = {
                                vm.uninstall(it)
                            }
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                }
                VerticalScrollbar(
                    modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
                    adapter = rememberScrollbarAdapter(
                        scrollState = state,
                        itemCount = itemCount + 1, // Plus toolbar,
                        averageItemSize = 37.dp // TextBox height + Spacer height
                    )
                )
            }
        }
    }
}

@Composable
fun ExtensionItem(
    extension: Extension,
    serverUrl: String,
    onInstallClicked: (Extension) -> Unit,
    onUninstallClicked: (Extension) -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth().height(64.dp).background(MaterialTheme.colors.background)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Spacer(Modifier.width(4.dp))
            KtorImage(extension.iconUrl(serverUrl), Modifier.size(60.dp))
            Spacer(Modifier.width(8.dp))
            Column {
                val title = buildAnnotatedString {
                    append("${extension.name} ")
                    val mediumColor = MaterialTheme.colors.onBackground.copy(alpha = ContentAlpha.medium)
                    withStyle(SpanStyle(fontSize = 12.sp, color = mediumColor)) { append("v${extension.versionName}") }
                }
                Text(title, fontSize = 26.sp, color = MaterialTheme.colors.onBackground)
                Row {
                    Text(extension.lang.toUpperCase(), fontSize = 14.sp, color = MaterialTheme.colors.onBackground)
                    if (extension.nsfw) {
                        Spacer(Modifier.width(4.dp))
                        Text("18+", fontSize = 14.sp, color = Color.Red)
                    }
                }
            }
        }
        Button(
            {
                if (extension.installed) onUninstallClicked(extension) else onInstallClicked(extension)
            },
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            Text(if (extension.installed) "Uninstall" else "Install")
        }
    }
}
