/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.updates

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ca.gosyer.data.models.Chapter
import ca.gosyer.ui.base.components.ChapterDownloadIcon
import ca.gosyer.ui.base.components.ChapterDownloadItem
import ca.gosyer.ui.base.components.LoadingScreen
import ca.gosyer.ui.base.components.MangaListItem
import ca.gosyer.ui.base.components.MangaListItemColumn
import ca.gosyer.ui.base.components.MangaListItemImage
import ca.gosyer.ui.base.components.MangaListItemSubtitle
import ca.gosyer.ui.base.components.MangaListItemTitle
import ca.gosyer.ui.base.components.Toolbar
import ca.gosyer.ui.base.components.mangaAspectRatio
import ca.gosyer.ui.base.resources.stringResource
import ca.gosyer.ui.base.vm.viewModel

@Composable
fun UpdatesMenu(
    openChapter: (Int, Long) -> Unit,
    openManga: (Long) -> Unit
) {
    val vm = viewModel<UpdatesMenuViewModel>()
    val serverUrl by vm.serverUrl.collectAsState()
    val isLoading by vm.isLoading.collectAsState()
    val updates by vm.updates.collectAsState()
    Column {
        Toolbar(stringResource("location_updates"), closable = false)
        if (isLoading || updates.isEmpty()) {
            LoadingScreen(isLoading)
        } else {
            LazyColumn {
                items(updates) {
                    val manga = it.manga!!
                    val chapter = it.chapter
                    UpdatesItem(
                        serverUrl,
                        it,
                        onClickItem = { openChapter(chapter.index, chapter.mangaId) },
                        onClickCover = { openManga(manga.id) },
                        onClickDownload = vm::downloadChapter,
                        onClickDeleteDownload = vm::deleteDownload
                    )
                }
            }
        }
    }
}

@Composable
fun UpdatesItem(
    serverUrl: String,
    chapterDownloadItem: ChapterDownloadItem,
    onClickItem: () -> Unit,
    onClickCover: () -> Unit,
    onClickDownload: (Chapter) -> Unit,
    onClickDeleteDownload: (Chapter) -> Unit
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
            imageUrl = manga.cover(serverUrl)
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
            onClickDeleteDownload
        )
    }
}
