/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.extensions.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.Button
import androidx.compose.material.ContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ca.gosyer.jui.core.lang.getDefault
import ca.gosyer.jui.core.lang.getDisplayName
import ca.gosyer.jui.core.lang.uppercase
import ca.gosyer.jui.data.models.Extension
import ca.gosyer.jui.i18n.MR
import ca.gosyer.jui.presentation.build.BuildKonfig
import ca.gosyer.jui.ui.base.dialog.getMaterialDialogProperties
import ca.gosyer.jui.ui.base.navigation.ActionItem
import ca.gosyer.jui.ui.base.navigation.Toolbar
import ca.gosyer.jui.uicore.components.LoadingScreen
import ca.gosyer.jui.uicore.components.VerticalScrollbar
import ca.gosyer.jui.uicore.components.rememberScrollbarAdapter
import ca.gosyer.jui.uicore.components.scrollbarPadding
import ca.gosyer.jui.uicore.image.KamelImage
import ca.gosyer.jui.uicore.resources.stringResource
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.MaterialDialogState
import com.vanpra.composematerialdialogs.rememberMaterialDialogState
import com.vanpra.composematerialdialogs.title
import io.fluidsonic.locale.Locale
import io.kamel.image.lazyPainterResource

@Composable
fun ExtensionsScreenContent(
    extensions: Map<String, List<Extension>>,
    isLoading: Boolean,
    query: String?,
    setQuery: (String) -> Unit,
    enabledLangs: Set<String>,
    availableLangs: Set<String>,
    setEnabledLanguages: (Set<String>) -> Unit,
    installExtension: (Extension) -> Unit,
    updateExtension: (Extension) -> Unit,
    uninstallExtension: (Extension) -> Unit
) {
    val languageDialogState = rememberMaterialDialogState()
    Scaffold(
        topBar = {
            ExtensionsToolbar(
                query,
                setQuery,
                languageDialogState::show
            )
        }
    ) { padding ->
        if (isLoading) {
            LoadingScreen()
        } else {
            val state = rememberLazyListState()

            Box(Modifier.fillMaxSize().padding(padding)) {
                LazyColumn(Modifier.fillMaxSize(), state) {
                    extensions.forEach { (header, items) ->
                        item(key = header) {
                            Text(
                                header,
                                style = MaterialTheme.typography.h6,
                                modifier = Modifier.padding(16.dp, 16.dp, 16.dp, 4.dp)
                            )
                        }
                        items(
                            items,
                            key = { it.pkgName }
                        ) { extension ->
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
                        .scrollbarPadding(),
                    adapter = rememberScrollbarAdapter(state)
                )
            }
        }
    }
    LanguageDialog(languageDialogState, enabledLangs, availableLangs, setEnabledLanguages)
}

@Composable
fun ExtensionsToolbar(
    searchText: String?,
    search: (String) -> Unit,
    openLanguageDialog: () -> Unit
) {
    Toolbar(
        stringResource(MR.strings.location_extensions),
        searchText = searchText,
        search = search,
        actions = {
            getActionItems(openLanguageDialog)
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
                        Text(stringResource(MR.strings.obsolete), fontSize = 14.sp, color = Color.Red)
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

@Composable
fun LanguageDialog(
    state: MaterialDialogState,
    enabledLangs: Set<String>,
    availableLangs: Set<String>,
    setLangs: (Set<String>) -> Unit
) {
    val modifiedLangs = remember(enabledLangs) { enabledLangs.toMutableStateList() }
    MaterialDialog(
        state,
        buttons = {
            positiveButton(stringResource(MR.strings.action_ok)) {
                setLangs(modifiedLangs.toSet())
            }
            negativeButton(stringResource(MR.strings.action_cancel))
        },
        properties = getMaterialDialogProperties(),
    ) {
        title(BuildKonfig.NAME)
        Box {
            val locale = remember { Locale.getDefault() }
            val listState = rememberLazyListState()
            LazyColumn(Modifier.fillMaxWidth(), listState) {
                items(availableLangs.toList()) { lang ->
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .height(48.dp)
                            .clickable {
                                if (lang in modifiedLangs) {
                                    modifiedLangs -= lang
                                } else {
                                    modifiedLangs += lang
                                }
                            }
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val langName by derivedStateOf {
                            Locale.forLanguageTag(lang).getDisplayName(locale).ifBlank { lang }
                        }
                        Text(langName)
                        Switch(
                            checked = lang in modifiedLangs,
                            onCheckedChange = null
                        )
                    }
                }
            }
            VerticalScrollbar(
                rememberScrollbarAdapter(listState),
                Modifier.align(Alignment.CenterEnd)
                    .fillMaxHeight()
                    .scrollbarPadding()
            )
        }
    }
}

@Stable
@Composable
private fun getActionItems(
    openLanguageDialog: () -> Unit
): List<ActionItem> {
    return listOf(
        ActionItem(
            stringResource(MR.strings.enabled_languages),
            Icons.Rounded.Translate,
            doAction = openLanguageDialog
        )
    )
}
