/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.sources

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.TooltipArea
import androidx.compose.foundation.TooltipPlacement
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import ca.gosyer.build.BuildConfig
import ca.gosyer.data.models.Source
import ca.gosyer.ui.base.components.KamelImage
import ca.gosyer.ui.base.components.combinedMouseClickable
import dev.icerock.moko.resources.compose.stringResource
import ca.gosyer.i18n.MR
import ca.gosyer.ui.base.vm.viewModel
import ca.gosyer.ui.manga.openMangaMenu
import ca.gosyer.ui.sources.components.SourceHomeScreen
import ca.gosyer.ui.sources.components.SourceScreen
import ca.gosyer.ui.sources.settings.openSourceSettingsMenu
import ca.gosyer.util.compose.ThemedWindow
import ca.gosyer.util.lang.launchApplication
import com.github.zsoltk.compose.savedinstancestate.Bundle
import com.github.zsoltk.compose.savedinstancestate.BundleScope
import com.github.zsoltk.compose.savedinstancestate.LocalSavedInstanceState
import io.kamel.image.lazyPainterResource
import kotlinx.coroutines.DelicateCoroutinesApi

@OptIn(DelicateCoroutinesApi::class)
fun openSourcesMenu() {
    launchApplication {
        ThemedWindow(::exitApplication, title = BuildConfig.NAME) {
            Surface {
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
    val sourceTabs by vm.sourceTabs.collectAsState()
    val selectedSourceTab by vm.selectedSourceTab.collectAsState()
    Row {
        SourcesSideMenu(
            sourceTabs = sourceTabs,
            onSourceTabClick = vm::selectTab,
            onCloseSourceTabClick = vm::closeTab
        )

        SourceTab(
            onLoadSources = vm::setLoadedSources,
            onSourceClicked = vm::addTab,
            selectedSourceTab = selectedSourceTab,
            onMangaClick = onMangaClick,
            onCloseSourceTabClick = vm::closeTab,
            onSourceSettingsClick = onSourceSettingsClick
        )
    }
}

@Composable
fun SourcesSideMenu(
    sourceTabs: List<Source?>,
    onSourceTabClick: (Source?) -> Unit,
    onCloseSourceTabClick: (Source) -> Unit
) {
    Surface(elevation = 1.dp) {
        Box {
            val state = rememberLazyListState()
            LazyColumn(Modifier.fillMaxHeight().width(64.dp), state) {
                items(sourceTabs) { source ->
                    TooltipArea(
                        {
                            Surface(
                                modifier = Modifier.shadow(4.dp),
                                shape = RoundedCornerShape(4.dp),
                                elevation = 4.dp
                            ) {
                                Text(source?.name ?: stringResource(MR.strings.sources_home), modifier = Modifier.padding(10.dp))
                            }
                        },
                        modifier = Modifier.size(64.dp),
                        tooltipPlacement = TooltipPlacement.CursorPoint(
                            offset = DpOffset(0.dp, 16.dp)
                        )
                    ) {
                        Box(Modifier.fillMaxSize()) {
                            val modifier = Modifier
                                .combinedMouseClickable(
                                    onClick = {
                                        onSourceTabClick(source)
                                    },
                                    onMiddleClick = {
                                        if (source != null) {
                                            onCloseSourceTabClick(source)
                                        }
                                    }
                                )
                                .requiredSize(50.dp)
                                .align(Alignment.Center)
                            if (source != null) {
                                Box(Modifier.align(Alignment.Center)) {
                                    KamelImage(
                                        lazyPainterResource(source, filterQuality = FilterQuality.Medium),
                                        source.displayName,
                                        modifier
                                    )
                                }
                            } else {
                                Icon(Icons.Rounded.Home, stringResource(MR.strings.sources_home), modifier = modifier)
                            }
                        }
                    }
                }
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

@Composable
fun SourceTab(
    onLoadSources: (List<Source>) -> Unit,
    onSourceClicked: (Source) -> Unit,
    selectedSourceTab: Source?,
    onMangaClick: (Long) -> Unit,
    onCloseSourceTabClick: (Source) -> Unit,
    onSourceSettingsClick: (Long) -> Unit
) {
    Crossfade(selectedSourceTab) { selectedSource ->
        BundleScope(selectedSource?.id.toString(), autoDispose = false) {
            if (selectedSource != null) {
                SourceScreen(
                    bundle = it,
                    source = selectedSource,
                    onMangaClick = onMangaClick,
                    onCloseSourceTabClick = onCloseSourceTabClick,
                    onSourceSettingsClick = onSourceSettingsClick
                )
            } else {
                SourceHomeScreen(
                    bundle = it,
                    onAddSource = onSourceClicked,
                    onLoadSources = onLoadSources
                )
            }
        }
    }
}
