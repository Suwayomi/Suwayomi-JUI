/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.manga.components

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.Card
import androidx.compose.material.Checkbox
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEach
import ca.gosyer.data.models.Category
import ca.gosyer.data.models.Manga
import ca.gosyer.ui.base.WindowDialog
import ca.gosyer.uicore.image.KamelImage
import com.google.accompanist.flowlayout.FlowRow
import io.kamel.image.lazyPainterResource
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun MangaItem(manga: Manga) {
    BoxWithConstraints(Modifier.padding(8.dp)) {
        if (maxWidth > 600.dp) {
            Row {
                Cover(manga, Modifier.width(300.dp))
                Spacer(Modifier.width(16.dp))
                MangaInfo(manga)
            }
        } else {
            Column {
                Cover(manga, Modifier.align(Alignment.CenterHorizontally))
                Spacer(Modifier.height(16.dp))
                MangaInfo(manga)
            }
        }
    }
}

@Composable
private fun Cover(manga: Manga, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier then Modifier
            .padding(4.dp),
        shape = RoundedCornerShape(4.dp)
    ) {
        KamelImage(
            lazyPainterResource(manga, filterQuality = FilterQuality.Medium),
            manga.title,
            Modifier.fillMaxSize()
        )
    }
}

@Composable
private fun MangaInfo(manga: Manga, modifier: Modifier = Modifier) {
    SelectionContainer {
        Column(modifier) {
            Text(manga.title, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            if (!manga.author.isNullOrEmpty()) {
                Text(manga.author!!, fontSize = 18.sp)
            }
            if (!manga.artist.isNullOrEmpty() && manga.artist != manga.author) {
                Text(manga.artist!!, fontSize = 18.sp)
            }
            if (!manga.description.isNullOrEmpty()) {
                Text(manga.description!!)
            }
            if (manga.genre.isNotEmpty()) {
                FlowRow {
                    manga.genre.fastForEach {
                        Chip(it)
                    }
                }
                // Text(manga.genre.joinToString())
            }
        }
    }
}

@Composable
private fun Chip(text: String) {
    Box(Modifier.padding(horizontal = 4.dp, vertical = 2.dp)) {
        Card(
            shape = RoundedCornerShape(50),
            elevation = 4.dp
        ) {
            Text(text, Modifier.align(Alignment.Center).padding(8.dp))
        }
    }
}

fun openCategorySelectDialog(
    categories: List<Category>,
    oldCategories: List<Category>,
    onPositiveClick: (List<Category>, List<Category>) -> Unit
) {
    val enabledCategoriesFlow = MutableStateFlow(oldCategories)
    WindowDialog(
        "Select Categories",
        onPositiveButton = { onPositiveClick(enabledCategoriesFlow.value, oldCategories) }
    ) {
        val enabledCategories by enabledCategoriesFlow.collectAsState()
        val state = rememberLazyListState()
        Box {
            LazyColumn(state = state) {
                items(categories) { category ->
                    Row(
                        Modifier.fillMaxWidth().padding(8.dp)
                            .clickable {
                                if (category in enabledCategories) {
                                    enabledCategoriesFlow.value -= category
                                } else {
                                    enabledCategoriesFlow.value += category
                                }
                            },
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(category.name, style = MaterialTheme.typography.subtitle1)
                        Checkbox(
                            category in enabledCategories,
                            onCheckedChange = null
                        )
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
