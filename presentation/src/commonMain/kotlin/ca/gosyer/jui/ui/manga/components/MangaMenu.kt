/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.manga.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEach
import ca.gosyer.jui.domain.category.model.Category
import ca.gosyer.jui.domain.manga.model.Manga
import ca.gosyer.jui.i18n.MR
import ca.gosyer.jui.ui.base.dialog.getMaterialDialogProperties
import ca.gosyer.jui.ui.base.model.StableHolder
import ca.gosyer.jui.uicore.components.VerticalScrollbar
import ca.gosyer.jui.uicore.components.mangaAspectRatio
import ca.gosyer.jui.uicore.components.rememberScrollbarAdapter
import ca.gosyer.jui.uicore.components.scrollbarPadding
import ca.gosyer.jui.uicore.image.ImageLoaderImage
import ca.gosyer.jui.uicore.resources.stringResource
import com.google.accompanist.flowlayout.FlowRow
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.MaterialDialogState
import com.vanpra.composematerialdialogs.listItemsMultiChoice
import com.vanpra.composematerialdialogs.title
import kotlinx.collections.immutable.ImmutableList

@Composable
fun MangaItem(mangaHolder: StableHolder<Manga>) {
    BoxWithConstraints(Modifier.padding(8.dp)) {
        if (maxWidth > 720.dp) {
            Row {
                Cover(mangaHolder, Modifier.width(300.dp))
                Spacer(Modifier.width(16.dp))
                MangaInfo(mangaHolder)
            }
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Cover(
                    mangaHolder,
                    Modifier.heightIn(120.dp, 300.dp)
                )
                Spacer(Modifier.height(16.dp))
                MangaInfo(mangaHolder)
            }
        }
    }
}

@Composable
private fun Cover(mangaHolder: StableHolder<Manga>, modifier: Modifier = Modifier) {
    val manga = mangaHolder.item
    ImageLoaderImage(
        data = manga,
        contentDescription = manga.title,
        modifier = modifier,
        errorModifier = modifier then Modifier
            .aspectRatio(
                ratio = mangaAspectRatio,
                matchHeightConstraintsFirst = true
            ),
        filterQuality = FilterQuality.Medium
    )
}

@Composable
private fun MangaInfo(mangaHolder: StableHolder<Manga>, modifier: Modifier = Modifier) {
    val manga = mangaHolder.item
    SelectionContainer {
        Column(modifier) {
            Text(
                text = manga.title,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            if (!manga.author.isNullOrEmpty()) {
                Text(
                    text = manga.author!!,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(2.dp))
            }
            if (!manga.artist.isNullOrEmpty() && manga.artist != manga.author) {
                Text(
                    text = manga.artist!!,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(2.dp))
            }
            val sourceText = manga.source?.displayName ?: manga.sourceId.toString()
            Text(
                text = stringResource(manga.status.res) + " • " + sourceText,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(8.dp))
            if (!manga.description.isNullOrEmpty()) {
                Text(manga.description!!)
                Spacer(Modifier.height(8.dp))
            }
            if (manga.genre.isNotEmpty()) {
                FlowRow(mainAxisSpacing = 8.dp, crossAxisSpacing = 8.dp) {
                    manga.genre.fastForEach {
                        Chip(it)
                    }
                }
            }
        }
    }
}

@Composable
private fun Chip(text: String) {
    Card(
        shape = RoundedCornerShape(50),
        modifier = Modifier.defaultMinSize(minHeight = 32.dp)
            .height(IntrinsicSize.Min),
        contentColor = MaterialTheme.colors.onSurface.copy(alpha = 0.10F)
    ) {
        Box(
            modifier = Modifier.padding(start = 8.dp, end = 6.dp)
                .fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.body2,
                fontSize = 14.sp,
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.85F),
                maxLines = 1
            )
        }
    }
}

@Composable
fun CategorySelectDialog(
    state: MaterialDialogState,
    categories: ImmutableList<StableHolder<Category>>,
    oldCategories: ImmutableList<StableHolder<Category>>,
    onPositiveClick: (List<Category>, List<Category>) -> Unit
) {
    MaterialDialog(
        state,
        buttons = {
            positiveButton(stringResource(MR.strings.action_ok))
            negativeButton(stringResource(MR.strings.action_cancel))
        },
        properties = getMaterialDialogProperties()
    ) {
        title(stringResource(MR.strings.select_categories))

        Box {
            val listState = rememberLazyListState()
            listItemsMultiChoice(
                list = categories.map { it.item.name },
                state = listState,
                initialSelection = oldCategories.mapNotNull { category ->
                    categories.indexOfFirst { it.item.id == category.item.id }.takeUnless { it == -1 }
                }.toSet(),
                onCheckedChange = { indexes ->
                    onPositiveClick(indexes.map { categories[it].item }, oldCategories.map { it.item })
                }
            )
            VerticalScrollbar(
                modifier = Modifier.align(Alignment.CenterEnd)
                    .fillMaxHeight()
                    .scrollbarPadding(),
                adapter = rememberScrollbarAdapter(listState)
            )
        }
    }
}
