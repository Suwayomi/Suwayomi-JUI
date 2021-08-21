/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.sources

import androidx.compose.foundation.BoxWithTooltip
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import ca.gosyer.build.BuildConfig
import ca.gosyer.data.models.Source
import ca.gosyer.ui.base.components.ActionIcon
import ca.gosyer.ui.base.components.KtorImage
import ca.gosyer.ui.base.components.Toolbar
import ca.gosyer.ui.base.components.combinedMouseClickable
import ca.gosyer.ui.base.resources.stringResource
import ca.gosyer.ui.base.vm.viewModel
import ca.gosyer.ui.extensions.LanguageDialog
import ca.gosyer.ui.manga.openMangaMenu
import ca.gosyer.ui.sources.components.SourceHomeScreen
import ca.gosyer.ui.sources.components.SourceScreen
import ca.gosyer.ui.sources.settings.openSourceSettingsMenu
import ca.gosyer.util.compose.ThemedWindow
import ca.gosyer.util.lang.launchApplication
import com.github.zsoltk.compose.savedinstancestate.Bundle
import com.github.zsoltk.compose.savedinstancestate.BundleScope
import com.github.zsoltk.compose.savedinstancestate.LocalSavedInstanceState
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow

@OptIn(DelicateCoroutinesApi::class)
fun openSourcesMenu() {
    launchApplication {
        ThemedWindow(::exitApplication, title = BuildConfig.NAME) {
            CompositionLocalProvider(
                LocalSavedInstanceState provides Bundle()
            ) {
                SourcesMenu(
                    ::openSourceSettingsMenu,
                    ::openMangaMenu
                )
            }
        }
    }
}

@Composable
fun SourcesMenu(onSourceSettingsClick: (Long) -> Unit, onMangaClick: (Long) -> Unit) {
    SourcesMenu(LocalSavedInstanceState.current, onSourceSettingsClick, onMangaClick)
}

@Composable
fun SourcesMenu(bundle: Bundle, onSourceSettingsClick: (Long) -> Unit, onMangaClick: (Long) -> Unit) {
    val vm = viewModel<SourcesMenuViewModel> {
        bundle
    }
    val isLoading by vm.isLoading.collectAsState()
    val sources by vm.sources.collectAsState()
    val sourceTabs by vm.sourceTabs.collectAsState()
    val selectedSourceTab by vm.selectedSourceTab.collectAsState()
    val sourceSearchEnabled by vm.sourceSearchEnabled.collectAsState()
    val sourceSearchQuery by vm.sourceSearchQuery.collectAsState()
    val serverUrl by vm.serverUrl.collectAsState()

    Surface {
        Column {
            Toolbar(
                selectedSourceTab?.name ?: stringResource("location_sources"),
                closable = selectedSourceTab != null,
                onClose = {
                    selectedSourceTab?.let { vm.closeTab(it) }
                },
                searchText = if (sourceSearchEnabled) {
                    sourceSearchQuery
                } else null,
                search = if (sourceSearchEnabled) vm::search else null,
                searchSubmit = vm::submitSearch,
                actions = {
                    selectedSourceTab.let { selectedSource ->
                        if (selectedSource == null) {
                            ActionIcon(
                                {
                                    val enabledLangs = MutableStateFlow(vm.languages.value)
                                    LanguageDialog(enabledLangs, vm.getSourceLanguages().toList()) {
                                        vm.setEnabledLanguages(enabledLangs.value)
                                    }
                                },
                                stringResource("enabled_languages"),
                                Icons.Rounded.Translate
                            )
                        } else {
                            if (selectedSource.isConfigurable) {
                                ActionIcon(
                                    {
                                        onSourceSettingsClick(selectedSource.id)
                                    },
                                    stringResource("location_settings"),
                                    Icons.Rounded.Settings
                                )
                            }
                        }
                    }
                }
            )
            Row {
                Surface(elevation = 1.dp) {
                    LazyColumn(Modifier.fillMaxHeight().width(64.dp)) {
                        items(sourceTabs) { source ->
                            BoxWithTooltip(
                                {
                                    Surface(
                                        modifier = Modifier.shadow(4.dp),
                                        shape = RoundedCornerShape(4.dp),
                                        elevation = 4.dp
                                    ) {
                                        Text(source?.name ?: stringResource("sources_home"), modifier = Modifier.padding(10.dp))
                                    }
                                },
                                modifier = Modifier.size(64.dp)
                            ) {
                                Box(Modifier.fillMaxSize()) {
                                    val modifier = Modifier
                                        .combinedMouseClickable(
                                            onClick = {
                                                vm.selectTab(source)
                                            },
                                            onMiddleClick = {
                                                if (source != null) {
                                                    vm.closeTab(source)
                                                }
                                            }
                                        )
                                        .requiredSize(50.dp)
                                        .align(Alignment.Center)
                                    if (source != null) {
                                        KtorImage(source.iconUrl(serverUrl), modifier = modifier)
                                    } else {
                                        Icon(Icons.Rounded.Home, stringResource("sources_home"), modifier = modifier)
                                    }
                                }
                            }
                        }
                    }
                }

                val selectedSource: Source? = selectedSourceTab
                BundleScope(selectedSource?.id.toString(), autoDispose = false) {
                    if (selectedSource != null) {
                        SourceScreen(it, selectedSource, onMangaClick, vm::enableSearch, vm::setSearch)
                    } else {
                        SourceHomeScreen(isLoading, sources, serverUrl, vm::addTab)
                    }
                }
            }
        }
    }
}
