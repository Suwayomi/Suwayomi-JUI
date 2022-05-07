/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.updates.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ca.gosyer.jui.data.models.Chapter
import ca.gosyer.jui.i18n.MR
import ca.gosyer.jui.ui.base.chapter.ChapterDownloadIcon
import ca.gosyer.jui.ui.base.chapter.ChapterDownloadItem
import ca.gosyer.jui.ui.base.navigation.Toolbar
import ca.gosyer.jui.uicore.components.LoadingScreen
import ca.gosyer.jui.uicore.components.MangaListItem
import ca.gosyer.jui.uicore.components.MangaListItemColumn
import ca.gosyer.jui.uicore.components.MangaListItemImage
import ca.gosyer.jui.uicore.components.MangaListItemSubtitle
import ca.gosyer.jui.uicore.components.MangaListItemTitle
import ca.gosyer.jui.uicore.components.VerticalScrollbar
import ca.gosyer.jui.uicore.components.mangaAspectRatio
import ca.gosyer.jui.uicore.components.rememberScrollbarAdapter
import ca.gosyer.jui.uicore.resources.stringResource
import io.kamel.image.lazyPainterResource
import kotlinx.datetime.LocalDate

@Composable
fun UpdatesScreenContent(
    isLoading: Boolean,
    dateWithUpdates: List<Pair<LocalDate, List<ChapterDownloadItem>>>,
    loadNextPage: () -> Unit,
    openChapter: (Int, Long) -> Unit,
    openManga: (Long) -> Unit,
    downloadChapter: (Chapter) -> Unit,
    deleteDownloadedChapter: (Chapter) -> Unit,
    stopDownloadingChapter: (Chapter) -> Unit
) {
    Scaffold(
        topBar = {
            Toolbar(stringResource(MR.strings.location_updates))
        }
    ) {
        if (isLoading || dateWithUpdates.isEmpty()) {
            LoadingScreen(isLoading)
        } else {
            Box(Modifier.padding(it)) {
                val state = rememberLazyListState()
                LazyColumn(Modifier.fillMaxSize(), state) {
                    dateWithUpdates.forEachIndexed { index, (date, updates) ->
                        item {
                            Text(
                                text = date.toString(),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontWeight = FontWeight.Medium
                            )
                        }
                        itemsIndexed(updates) { itemIndex, item ->
                            LaunchedEffect(Unit) {
                                if (index == dateWithUpdates.lastIndex && itemIndex == updates.lastIndex) {
                                    loadNextPage()
                                }
                            }
                            val manga = item.manga!!
                            val chapter = item.chapter
                            UpdatesItem(
                                chapterDownloadItem = item,
                                onClickItem = { openChapter(chapter.index, chapter.mangaId) },
                                onClickCover = { openManga(manga.id) },
                                onClickDownload = downloadChapter,
                                onClickDeleteDownload = deleteDownloadedChapter,
                                onClickStopDownload = stopDownloadingChapter
                            )
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
}

@Composable
fun UpdatesItem(
    chapterDownloadItem: ChapterDownloadItem,
    onClickItem: () -> Unit,
    onClickCover: () -> Unit,
    onClickDownload: (Chapter) -> Unit,
    onClickDeleteDownload: (Chapter) -> Unit,
    onClickStopDownload: (Chapter) -> Unit
) {
    val manga = chapterDownloadItem.manga!!
    val chapter = chapterDownloadItem.chapter
    val alpha = if (chapter.read) 0.38f else 1f

    MangaListItem(
        modifier = Modifier
            .clickable(
                onClick = { onClickItem() }
            )
            .height(96.dp)
            .fillMaxWidth()
            .padding(end = 4.dp)
    ) {
        MangaListItemImage(
            modifier = Modifier
                .fillMaxHeight()
                .aspectRatio(mangaAspectRatio)
                .padding(start = 16.dp, top = 8.dp, bottom = 8.dp)
                .clip(MaterialTheme.shapes.medium)
                .clickable { onClickCover() },
            cover = lazyPainterResource(manga, filterQuality = FilterQuality.Medium),
            contentDescription = manga.title
        )
        MangaListItemColumn(
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp)
                .alpha(alpha)
        ) {
            MangaListItemTitle(
                text = manga.title,
                fontWeight = FontWeight.SemiBold
            )
            MangaListItemSubtitle(
                text = chapter.name
            )
        }

        ChapterDownloadIcon(
            chapterDownloadItem,
            onClickDownload,
            onClickStopDownload,
            onClickDeleteDownload,
        )
    }
}
