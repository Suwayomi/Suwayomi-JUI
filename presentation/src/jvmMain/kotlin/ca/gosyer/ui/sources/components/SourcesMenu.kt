/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.sources.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import ca.gosyer.data.models.Source
import ca.gosyer.i18n.MR
import ca.gosyer.ui.base.components.CursorPoint
import ca.gosyer.ui.base.components.TooltipArea
import ca.gosyer.ui.base.components.VerticalScrollbar
import ca.gosyer.ui.base.components.rememberScrollbarAdapter
import ca.gosyer.ui.sources.browse.SourceScreen
import ca.gosyer.ui.sources.home.SourceHomeScreen
import ca.gosyer.uicore.image.KamelImage
import ca.gosyer.uicore.resources.stringResource
import io.kamel.image.lazyPainterResource

expect fun Modifier.sourceSideMenuItem(
    onSourceTabClick: () -> Unit,
    onSourceCloseTabClick: () -> Unit
): Modifier

@Composable
fun SourcesMenu(
    sourceTabs: List<Source?>,
    selectedSourceTab: Source?,
    selectTab: (Source?) -> Unit,
    closeTab: (Source) -> Unit
) {
    val homeScreen = remember { SourceHomeScreen() }
    SourcesNavigator(
        homeScreen,
        removeSource = closeTab,
        selectSource = selectTab
    ) { navigator ->
        LaunchedEffect(selectedSourceTab) {
            navigator.current = if (selectedSourceTab == null) {
                homeScreen
            } else SourceScreen(selectedSourceTab)
        }
        BoxWithConstraints {
            if (maxWidth > 720.dp) {
                Row {
                    SourcesSideMenu(
                        sourceTabs = sourceTabs,
                        onSourceTabClick = selectTab,
                        onCloseSourceTabClick = {
                            closeTab(it)
                            navigator.stateHolder.removeState(it.id)
                        }
                    )

                    CurrentSource()
                }
            } else {
                CurrentSource()
            }
        }

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
                        tooltipPlacement = CursorPoint(
                            offset = DpOffset(0.dp, 16.dp)
                        )
                    ) {
                        Box(Modifier.fillMaxSize()) {
                            val modifier = Modifier
                                .sourceSideMenuItem(
                                    onSourceTabClick = {
                                        onSourceTabClick(source)
                                    },
                                    onSourceCloseTabClick = {
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
