/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.sources.components

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ca.gosyer.data.models.Source
import ca.gosyer.ui.base.components.KtorImage
import ca.gosyer.ui.base.components.LoadingScreen
import java.util.Locale

@Composable
fun SourceHomeScreen(
    isLoading: Boolean,
    sources: List<Source>,
    serverUrl: String,
    onSourceClicked: (Source) -> Unit
) {
    if (sources.isEmpty()) {
        LoadingScreen(isLoading)
    } else {
        Box(Modifier.fillMaxSize(), Alignment.TopCenter) {
            val state = rememberLazyListState()
            SourceCategory("all", sources, serverUrl, onSourceClicked, state)
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
                modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
                adapter = rememberScrollbarAdapter(
                    scrollState = state,
                    itemCount = sources.size,
                    averageItemSize = 12.dp // TextBox height + Spacer height
                )
            )
        }
    }
}

@Composable
fun SourceCategory(
    lang: String,
    sources: List<Source>,
    serverUrl: String,
    onSourceClicked: (Source) -> Unit,
    state: LazyListState
) {
    Column {
        Surface(elevation = 1.dp, modifier = Modifier.fillMaxWidth()) {
            Text(lang.uppercase(Locale.getDefault()), modifier = Modifier.align(Alignment.CenterHorizontally), color = MaterialTheme.colors.onBackground)
        }
        LazyVerticalGrid(GridCells.Adaptive(120.dp), state = state) {
            items(sources) { source ->
                SourceItem(
                    source,
                    serverUrl,
                    onSourceClicked = onSourceClicked
                )
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun SourceItem(
    source: Source,
    serverUrl: String,
    onSourceClicked: (Source) -> Unit
) {
    Column(
        Modifier.size(120.dp)
            .clickable {
                onSourceClicked(source)
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        KtorImage(source.iconUrl(serverUrl), Modifier.size(96.dp))
        Spacer(Modifier.height(4.dp))
        Text("${source.name} (${source.lang})", color = MaterialTheme.colors.onBackground)
    }
}
