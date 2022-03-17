/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.sources.home.components

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
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyVerticalGrid
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
import ca.gosyer.data.models.Source
import ca.gosyer.i18n.MR
import ca.gosyer.ui.base.components.TooltipArea
import ca.gosyer.ui.base.components.localeToString
import ca.gosyer.ui.base.navigation.ActionItem
import ca.gosyer.ui.base.navigation.Toolbar
import ca.gosyer.ui.extensions.components.LanguageDialog
import ca.gosyer.uicore.components.LoadingScreen
import ca.gosyer.uicore.components.VerticalScrollbar
import ca.gosyer.uicore.components.rememberScrollbarAdapter
import ca.gosyer.uicore.image.KamelImage
import ca.gosyer.uicore.resources.stringResource
import com.vanpra.composematerialdialogs.rememberMaterialDialogState
import io.kamel.image.lazyPainterResource

@Composable
fun SourceHomeScreenContent(
    onAddSource: (Source) -> Unit,
    isLoading: Boolean,
    sources: List<Source>,
    languages: Set<String>,
    sourceLanguages: Set<String>,
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
    ) {
        if (sources.isEmpty()) {
            LoadingScreen(isLoading)
        } else {
            Box(Modifier.fillMaxSize().padding(it), Alignment.TopCenter) {
                val state = rememberLazyListState()
                SourceCategory(sources, onAddSource, state)
                /*val sourcesByLang = sources.groupBy { it.lang.toLowerCase() }.toList()
                LazyColumn(state = state) {
                    items(sourcesByLang) { (lang, sources) ->
                        SourceCategory(
                            lang,
                            sources,
                            onSourceClicked = sourceClicked
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                }*/

                VerticalScrollbar(
                    modifier = Modifier.align(Alignment.CenterEnd)
                        .fillMaxHeight()
                        .padding(horizontal = 4.dp, vertical = 8.dp),
                    adapter = rememberScrollbarAdapter(state)
                )
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
fun SourceCategory(
    sources: List<Source>,
    onSourceClicked: (Source) -> Unit,
    state: LazyListState
) {
    BoxWithConstraints {
        if (maxWidth > 720.dp) {
            LazyVerticalGrid(GridCells.Adaptive(120.dp), state = state) {
                items(sources) { source ->
                    WideSourceItem(
                        source,
                        onSourceClicked = onSourceClicked
                    )
                }
            }
        } else {
            LazyColumn(state = state) {
                items(sources) { source ->
                    ThinSourceItem(
                        source,
                        onSourceClicked = onSourceClicked
                    )
                }
            }
        }
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
            KamelImage(lazyPainterResource(source, filterQuality = FilterQuality.Medium), source.displayName, Modifier.size(96.dp))
            Spacer(Modifier.height(4.dp))
            Text(
                "${source.name} (${source.displayLang.uppercase()})",
                color = MaterialTheme.colors.onBackground,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
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
        KamelImage(
            lazyPainterResource(source, filterQuality = FilterQuality.Medium),
            source.displayName,
            Modifier.fillMaxHeight()
                .aspectRatio(1F, true)
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
