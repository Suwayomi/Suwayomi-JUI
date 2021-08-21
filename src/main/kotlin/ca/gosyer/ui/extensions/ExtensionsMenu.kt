/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.extensions

import androidx.compose.foundation.ScrollbarAdapter
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
import androidx.compose.material.Surface
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.WindowSize
import androidx.compose.ui.window.rememberWindowState
import ca.gosyer.build.BuildConfig
import ca.gosyer.data.models.Extension
import ca.gosyer.ui.base.WindowDialog
import ca.gosyer.ui.base.components.ActionIcon
import ca.gosyer.ui.base.components.KtorImage
import ca.gosyer.ui.base.components.LoadingScreen
import ca.gosyer.ui.base.components.Toolbar
import ca.gosyer.ui.base.resources.stringResource
import ca.gosyer.ui.base.vm.viewModel
import ca.gosyer.util.compose.ThemedWindow
import ca.gosyer.util.compose.persistentLazyListState
import ca.gosyer.util.lang.launchApplication
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.Locale

@OptIn(DelicateCoroutinesApi::class)
fun openExtensionsMenu() {
    launchApplication {
        val state = rememberWindowState(size = WindowSize(550.dp, 700.dp))
        ThemedWindow(::exitApplication, state, title = BuildConfig.NAME) {
            ExtensionsMenu()
        }
    }
}

@Composable
fun ExtensionsMenu() {
    val vm = viewModel<ExtensionsMenuViewModel>()
    val extensions by vm.extensions.collectAsState()
    val isLoading by vm.isLoading.collectAsState()
    val serverUrl by vm.serverUrl.collectAsState()
    val search by vm.searchQuery.collectAsState()

    Surface(Modifier.fillMaxSize()) {
        if (isLoading) {
            LoadingScreen(isLoading)
        } else {
            val state = persistentLazyListState()

            Box(Modifier.fillMaxSize()) {
                LazyColumn(Modifier.fillMaxSize(), state) {
                    item {
                        Toolbar(
                            stringResource("location_extensions"),
                            closable = false,
                            searchText = search,
                            search = {
                                vm.search(it)
                            },
                            actions = {
                                ActionIcon(
                                    {
                                        val enabledLangs = MutableStateFlow(vm.enabledLangs.value)
                                        LanguageDialog(enabledLangs, vm.getSourceLanguages().toList()) {
                                            vm.setEnabledLanguages(enabledLangs.value)
                                        }
                                    },
                                    stringResource("enabled_languages"),
                                    Icons.Rounded.Translate
                                )
                            }
                        )
                    }
                    extensions.forEach { (header, items) ->
                        item {
                            Text(
                                header,
                                style = MaterialTheme.typography.h6,
                                modifier = Modifier.padding(16.dp, 16.dp, 16.dp, 4.dp)
                            )
                        }
                        items(items) { extension ->
                            ExtensionItem(
                                extension,
                                serverUrl,
                                onInstallClicked = vm::install,
                                onUpdateClicked = vm::update,
                                onUninstallClicked = vm::uninstall
                            )
                            Spacer(Modifier.height(8.dp))
                        }
                    }
                }
                VerticalScrollbar(
                    modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
                    adapter = rememberScrollbarAdapter(state)
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
    onUpdateClicked: (Extension) -> Unit,
    onUninstallClicked: (Extension) -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth().padding(end = 12.dp).height(50.dp).background(MaterialTheme.colors.background)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Spacer(Modifier.width(4.dp))
            KtorImage(extension.iconUrl(serverUrl), Modifier.size(50.dp))
            Spacer(Modifier.width(8.dp))
            Column {
                val title = buildAnnotatedString {
                    append("${extension.name} ")
                    val mediumColor = MaterialTheme.colors.onBackground.copy(alpha = ContentAlpha.medium)
                    withStyle(SpanStyle(fontSize = 12.sp, color = mediumColor)) { append("v${extension.versionName}") }
                }
                Text(title, fontSize = 18.sp, color = MaterialTheme.colors.onBackground)
                Row {
                    Text(extension.lang.uppercase(Locale.getDefault()), fontSize = 14.sp, color = MaterialTheme.colors.onBackground)
                    if (extension.isNsfw) {
                        Spacer(Modifier.width(4.dp))
                        Text("18+", fontSize = 14.sp, color = Color.Red)
                    }
                    if (extension.obsolete) {
                        Spacer(Modifier.width(4.dp))
                        Text("Obsolete", fontSize = 14.sp, color = Color.Red)
                    }
                }
            }
        }
        Button(
            {
                when {
                    extension.hasUpdate -> onUpdateClicked(extension)
                    extension.installed -> onUninstallClicked(extension)
                    else -> onInstallClicked(extension)
                }
            },
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            Text(
                when {
                    extension.hasUpdate -> stringResource("action_update")
                    extension.installed -> stringResource("action_uninstall")
                    else -> stringResource("action_install")
                }
            )
        }
    }
}

fun LanguageDialog(enabledLangsFlow: MutableStateFlow<Set<String>>, availableLangs: List<String>, setLangs: () -> Unit) {
    WindowDialog(BuildConfig.NAME, onPositiveButton = setLangs) {
        val locale = Locale.getDefault()
        val enabledLangs by enabledLangsFlow.collectAsState()
        val state = rememberLazyListState()
        Box {
            LazyColumn(Modifier.fillMaxWidth(), state) {
                items(availableLangs) { lang ->
                    Row {
                        val langName = remember(lang) {
                            Locale.forLanguageTag(lang)?.getDisplayName(locale) ?: lang
                        }
                        Text(langName)
                        Switch(
                            lang in enabledLangs,
                            {
                                if (it) {
                                    enabledLangsFlow.value += lang
                                } else {
                                    enabledLangsFlow.value -= lang
                                }
                            }
                        )
                    }
                }
                item { Spacer(Modifier.height(70.dp)) }
            }
            VerticalScrollbar(ScrollbarAdapter(state), Modifier.align(Alignment.CenterEnd).padding(8.dp))
        }
    }
}
