/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.sources.components

import androidx.compose.foundation.TooltipArea
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ca.gosyer.data.models.Source
import ca.gosyer.ui.base.components.KamelImage
import ca.gosyer.ui.base.components.LoadingScreen
import ca.gosyer.ui.base.components.TextActionIcon
import ca.gosyer.ui.base.components.Toolbar
import ca.gosyer.ui.base.resources.stringResource
import ca.gosyer.ui.base.vm.viewModel
import ca.gosyer.ui.extensions.LanguageDialog
import com.github.zsoltk.compose.savedinstancestate.Bundle
import io.kamel.image.lazyPainterResource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Composable
fun SourceHomeScreen(
    bundle: Bundle,
    onAddSource: (Source) -> Unit,
    onLoadSources: (List<Source>) -> Unit
) {
    val vm = viewModel<SourceHomeScreenViewModel> {
        bundle
    }
    val sources by vm.sources.collectAsState()
    val isLoading by vm.isLoading.collectAsState()
    LaunchedEffect(sources) {
        if (sources.isNotEmpty()) {
            onLoadSources(sources)
        }
    }

    if (sources.isEmpty()) {
        LoadingScreen(isLoading)
    } else {
        Column {
            SourceHomeScreenToolbar(
                vm.languages,
                vm::getSourceLanguages,
                vm::setEnabledLanguages
            )
            Box(Modifier.fillMaxSize(), Alignment.TopCenter) {
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
}

@Composable
fun SourceHomeScreenToolbar(
    sourceLanguages: StateFlow<Set<String>>,
    onGetEnabledLanguages: () -> Set<String>,
    onSetEnabledLanguages: (Set<String>) -> Unit
) {
    Toolbar(
        stringResource("location_sources"),
        closable = false,
        actions = {
            TextActionIcon(
                {
                    val enabledLangs = MutableStateFlow(sourceLanguages.value)
                    LanguageDialog(enabledLangs, onGetEnabledLanguages().toList()) {
                        onSetEnabledLanguages(enabledLangs.value)
                    }
                },
                stringResource("enabled_languages"),
                Icons.Rounded.Translate
            )
        }
    )
}

@Composable
fun SourceCategory(
    sources: List<Source>,
    onSourceClicked: (Source) -> Unit,
    state: LazyListState
) {
    LazyVerticalGrid(GridCells.Adaptive(120.dp), state = state) {
        items(sources) { source ->
            SourceItem(
                source,
                onSourceClicked = onSourceClicked
            )
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
fun SourceItem(
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
            Modifier.width(120.dp)
                .defaultMinSize(minHeight = 120.dp)
                .padding(8.dp)
                .clickable {
                    onSourceClicked(source)
                },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            KamelImage(lazyPainterResource(source, filterQuality = FilterQuality.Medium), source.displayName, Modifier.size(96.dp))
            Spacer(Modifier.height(4.dp))
            Text(
                "${source.name} (${source.lang.uppercase()})",
                color = MaterialTheme.colors.onBackground,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
