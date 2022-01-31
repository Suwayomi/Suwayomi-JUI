/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.extensions.components

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
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ca.gosyer.data.models.Extension
import ca.gosyer.i18n.MR
import ca.gosyer.presentation.build.BuildKonfig
import ca.gosyer.ui.base.WindowDialog
import ca.gosyer.ui.base.navigation.TextActionIcon
import ca.gosyer.ui.base.navigation.Toolbar
import ca.gosyer.uicore.components.LoadingScreen
import ca.gosyer.uicore.image.KamelImage
import ca.gosyer.uicore.resources.stringResource
import io.kamel.image.lazyPainterResource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Locale

@Composable
fun ExtensionsScreenContent(
    extensions: Map<String, List<Extension>>,
    isLoading: Boolean,
    query: String?,
    setQuery: (String) -> Unit,
    enabledLangs: StateFlow<Set<String>>,
    getSourceLanguages: () -> Set<String>,
    setEnabledLanguages: (Set<String>) -> Unit,
    installExtension: (Extension) -> Unit,
    updateExtension: (Extension) -> Unit,
    uninstallExtension: (Extension) -> Unit
) {
    if (isLoading) {
        Column {
            ExtensionsToolbar(
                query,
                setQuery,
                enabledLangs,
                getSourceLanguages,
                setEnabledLanguages
            )
            LoadingScreen(isLoading)
        }
    } else {
        val state = rememberLazyListState()

        Box(Modifier.fillMaxSize()) {
            LazyColumn(Modifier.fillMaxSize(), state) {
                item {
                    ExtensionsToolbar(
                        query,
                        setQuery,
                        enabledLangs,
                        getSourceLanguages,
                        setEnabledLanguages
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
                            onInstallClicked = installExtension,
                            onUpdateClicked = updateExtension,
                            onUninstallClicked = uninstallExtension
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
            VerticalScrollbar(
                modifier = Modifier.align(Alignment.CenterEnd)
                    .fillMaxHeight()
                    .padding(horizontal = 4.dp, vertical = 8.dp),
                adapter = rememberScrollbarAdapter(state)
            )
        }
    }
}

@Composable
fun ExtensionsToolbar(
    searchText: String?,
    search: (String) -> Unit,
    currentEnabledLangs: StateFlow<Set<String>>,
    getSourceLanguages: () -> Set<String>,
    setEnabledLanguages: (Set<String>) -> Unit
) {
    Toolbar(
        stringResource(MR.strings.location_extensions),
        closable = false,
        searchText = searchText,
        search = search,
        actions = {
            TextActionIcon(
                {
                    val enabledLangs = MutableStateFlow(currentEnabledLangs.value)
                    LanguageDialog(enabledLangs, getSourceLanguages().toList()) {
                        setEnabledLanguages(enabledLangs.value)
                    }
                },
                stringResource(MR.strings.enabled_languages),
                Icons.Rounded.Translate
            )
        }
    )
}

@Composable
fun ExtensionItem(
    extension: Extension,
    onInstallClicked: (Extension) -> Unit,
    onUpdateClicked: (Extension) -> Unit,
    onUninstallClicked: (Extension) -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth().padding(end = 12.dp).height(50.dp).background(MaterialTheme.colors.background)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Spacer(Modifier.width(4.dp))
            KamelImage(lazyPainterResource(extension, filterQuality = FilterQuality.Medium), extension.name, Modifier.size(50.dp))
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
                    extension.hasUpdate -> stringResource(MR.strings.action_update)
                    extension.installed -> stringResource(MR.strings.action_uninstall)
                    else -> stringResource(MR.strings.action_install)
                }
            )
        }
    }
}

fun LanguageDialog(enabledLangsFlow: MutableStateFlow<Set<String>>, availableLangs: List<String>, setLangs: () -> Unit) {
    WindowDialog(BuildKonfig.NAME, onPositiveButton = setLangs) {
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
            VerticalScrollbar(
                rememberScrollbarAdapter(state),
                Modifier.align(Alignment.CenterEnd)
                    .fillMaxHeight()
                    .padding(horizontal = 4.dp, vertical = 8.dp)
            )
        }
    }
}
