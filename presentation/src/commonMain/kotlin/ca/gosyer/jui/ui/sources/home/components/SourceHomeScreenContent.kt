/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.sources.home.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ca.gosyer.jui.domain.source.model.Source
import ca.gosyer.jui.i18n.MR
import ca.gosyer.jui.ui.base.components.TooltipArea
import ca.gosyer.jui.ui.base.components.localeToString
import ca.gosyer.jui.ui.base.navigation.ActionItem
import ca.gosyer.jui.ui.base.navigation.Toolbar
import ca.gosyer.jui.ui.extensions.components.LanguageDialog
import ca.gosyer.jui.ui.sources.home.SourceUI
import ca.gosyer.jui.uicore.components.LoadingScreen
import ca.gosyer.jui.uicore.components.VerticalScrollbar
import ca.gosyer.jui.uicore.components.rememberScrollbarAdapter
import ca.gosyer.jui.uicore.components.rememberVerticalScrollbarAdapter
import ca.gosyer.jui.uicore.components.scrollbarPadding
import ca.gosyer.jui.uicore.image.ImageLoaderImage
import ca.gosyer.jui.uicore.resources.stringResource
import com.vanpra.composematerialdialogs.rememberMaterialDialogState

@Composable
fun SourceHomeScreenContent(
    onAddSource: (Source) -> Unit,
    isLoading: Boolean,
    sources: List<SourceUI>,
    languages: Set<String>,
    sourceLanguages: List<String>,
    setEnabledLanguages: (Set<String>) -> Unit,
    query: String,
    setQuery: (String) -> Unit,
    submitSearch: (String) -> Unit
) {
    val languageDialogState = rememberMaterialDialogState()
    Scaffold(
        topBar = {
            SourceHomeScreenToolbar(
                openEnabledLanguagesClick = languageDialogState::show,
                query = query,
                setQuery = setQuery,
                submitSearch = submitSearch
            )
        }
    ) { padding ->
        if (sources.isEmpty()) {
            LoadingScreen(isLoading)
        } else {
            BoxWithConstraints(Modifier.fillMaxSize().padding(padding), Alignment.TopCenter) {
                if (maxWidth > 720.dp) {
                    WideSourcesMenu(sources, onAddSource)
                } else {
                    ThinSourcesMenu(sources, onAddSource)
                }
            }
        }
    }
    LanguageDialog(languageDialogState, languages, sourceLanguages, setEnabledLanguages)
}

@Composable
fun SourceHomeScreenToolbar(
    openEnabledLanguagesClick: () -> Unit,
    query: String,
    setQuery: (String) -> Unit,
    submitSearch: (String) -> Unit
) {
    Toolbar(
        stringResource(MR.strings.location_sources),
        actions = {
            getActionItems(
                openEnabledLanguagesClick = openEnabledLanguagesClick
            )
        },
        searchText = query,
        search = setQuery,
        searchSubmit = {
            if (query.isNotBlank()) {
                submitSearch(query)
            }
        }
    )
}

@Composable
fun WideSourcesMenu(
    sources: List<SourceUI>,
    onAddSource: (Source) -> Unit
) {
    Box {
        val state = rememberLazyGridState()
        val cells = GridCells.Adaptive(120.dp)
        LazyVerticalGrid(cells, state = state, modifier = Modifier.fillMaxSize()) {
            items(
                sources,
                contentType = {
                    when (it) {
                        is SourceUI.Header -> "header"
                        is SourceUI.SourceItem -> "source"
                    }
                },
                key = {
                    when (it) {
                        is SourceUI.Header -> it.header
                        is SourceUI.SourceItem -> it.source.id
                    }
                },
                span = {
                    when (it) {
                        is SourceUI.Header -> GridItemSpan(maxLineSpan)
                        is SourceUI.SourceItem -> GridItemSpan(1)
                    }
                }
            ) { sourceUI ->
                when (sourceUI) {
                    is SourceUI.Header -> Text(
                        sourceUI.header,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                    is SourceUI.SourceItem -> WideSourceItem(
                        sourceUI.source,
                        onSourceClicked = onAddSource
                    )
                }

            }
        }
        VerticalScrollbar(
            modifier = Modifier.align(Alignment.CenterEnd)
                .fillMaxHeight()
                .scrollbarPadding(),
            adapter = rememberVerticalScrollbarAdapter(state, cells)
        )
    }
}

@Composable
fun WideSourceItem(
    source: Source,
    onSourceClicked: (Source) -> Unit
) {
    TooltipArea(
        {
            Surface(
                modifier = Modifier.shadow(4.dp),
                shape = RoundedCornerShape(4.dp),
                elevation = 4.dp
            ) {
                Text(source.name, modifier = Modifier.padding(10.dp))
            }
        }
    ) {
        Column(
            Modifier.padding(8.dp)
                .clickable {
                    onSourceClicked(source)
                },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ImageLoaderImage(
                data = source,
                contentDescription = source.displayName,
                modifier = Modifier.size(96.dp),
                filterQuality = FilterQuality.Medium
            )
            Spacer(Modifier.height(4.dp))
            Text(
                source.name,
                color = MaterialTheme.colors.onBackground,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
@Composable
fun ThinSourcesMenu(
    sources: List<SourceUI>,
    onAddSource: (Source) -> Unit
) {
    Box {
        val state = rememberLazyListState()
        LazyColumn(state = state, modifier = Modifier.fillMaxSize()) {
            items(
                sources,
                contentType = {
                    when (it) {
                        is SourceUI.Header -> "header"
                        is SourceUI.SourceItem -> "source"
                    }
                },
                key = {
                    when (it) {
                        is SourceUI.Header -> it.header
                        is SourceUI.SourceItem -> it.source.id
                    }
                }
            ) { sourceUI ->
                when (sourceUI) {
                    is SourceUI.Header -> Text(
                        sourceUI.header,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                    is SourceUI.SourceItem -> ThinSourceItem(
                        sourceUI.source,
                        onSourceClicked = onAddSource
                    )
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

@Composable
fun ThinSourceItem(
    source: Source,
    onSourceClicked: (Source) -> Unit
) {
    Row(
        Modifier.fillMaxWidth()
            .height(64.dp)
            .clickable(onClick = { onSourceClicked(source) })
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ImageLoaderImage(
            source,
            source.displayName,
            Modifier.fillMaxHeight()
                .aspectRatio(1F, true),
            filterQuality = FilterQuality.Medium
        )
        Spacer(Modifier.width(8.dp))
        Column {
            Text(
                source.name,
                color = MaterialTheme.colors.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 14.sp
            )
            Text(
                localeToString(source.displayLang),
                color = MaterialTheme.colors.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
@Stable
private fun getActionItems(
    openEnabledLanguagesClick: () -> Unit
): List<ActionItem> {
    return listOf(
        ActionItem(
            stringResource(MR.strings.enabled_languages),
            Icons.Rounded.Translate,
            doAction = openEnabledLanguagesClick
        )
    )
}
