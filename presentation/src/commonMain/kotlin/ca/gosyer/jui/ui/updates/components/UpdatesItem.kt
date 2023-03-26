/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.updates.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ca.gosyer.jui.domain.chapter.model.Chapter
import ca.gosyer.jui.ui.base.chapter.ChapterDownloadIcon
import ca.gosyer.jui.ui.base.chapter.ChapterDownloadItem
import ca.gosyer.jui.uicore.components.MangaListItem
import ca.gosyer.jui.uicore.components.MangaListItemColumn
import ca.gosyer.jui.uicore.components.MangaListItemImage
import ca.gosyer.jui.uicore.components.MangaListItemSubtitle
import ca.gosyer.jui.uicore.components.MangaListItemTitle
import ca.gosyer.jui.uicore.components.mangaAspectRatio
import ca.gosyer.jui.uicore.components.selectedBackground

expect fun Modifier.updatesItemModifier(
    onClick: () -> Unit,
    markRead: (() -> Unit)?,
    markUnread: (() -> Unit)?,
    bookmarkChapter: (() -> Unit)?,
    unBookmarkChapter: (() -> Unit)?,
    onSelectChapter: (() -> Unit)?,
    onUnselectChapter: (() -> Unit)?,
): Modifier

@Composable
fun UpdatesItem(
    chapterDownloadItem: ChapterDownloadItem,
    onClickItem: () -> Unit,
    markRead: (Long) -> Unit,
    markUnread: (Long) -> Unit,
    bookmarkChapter: (Long) -> Unit,
    unBookmarkChapter: (Long) -> Unit,
    onSelectChapter: (Long) -> Unit,
    onUnselectChapter: (Long) -> Unit,
    onClickCover: () -> Unit,
    onClickDownload: (Chapter) -> Unit,
    onClickDeleteDownload: (Chapter) -> Unit,
    onClickStopDownload: (Chapter) -> Unit,
) {
    val manga = chapterDownloadItem.manga!!
    val chapter = chapterDownloadItem.chapter
    val alpha = if (chapter.read) 0.38f else 1f
    val isSelected by chapterDownloadItem.isSelected.collectAsState()

    MangaListItem(
        modifier = Modifier
            .selectedBackground(isSelected)
            .updatesItemModifier(
                onClick = onClickItem,
                markRead = { markRead(chapter.id) }.takeUnless { chapter.read },
                markUnread = { markUnread(chapter.id) }.takeIf { chapter.read },
                bookmarkChapter = { bookmarkChapter(chapter.id) }.takeUnless { chapter.bookmarked },
                unBookmarkChapter = { unBookmarkChapter(chapter.id) }.takeIf { chapter.bookmarked },
                onSelectChapter = { onSelectChapter(chapter.id) }.takeUnless { isSelected },
                onUnselectChapter = { onUnselectChapter(chapter.id) }.takeIf { isSelected },
            )
            .height(96.dp)
            .fillMaxWidth()
            .padding(end = 4.dp),
    ) {
        MangaListItemImage(
            modifier = Modifier
                .fillMaxHeight()
                .aspectRatio(mangaAspectRatio)
                .padding(start = 16.dp, top = 8.dp, bottom = 8.dp)
                .clip(MaterialTheme.shapes.medium)
                .clickable { onClickCover() },
            data = manga,
            contentDescription = manga.title,
        )
        val textColor = if (chapter.bookmarked && !chapter.read) {
            MaterialTheme.colors.primary
        } else {
            MaterialTheme.colors.onSurface.copy(
                alpha = if (chapter.read) ContentAlpha.disabled else ContentAlpha.medium,
            )
        }
        MangaListItemColumn(
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp)
                .alpha(alpha),
        ) {
            MangaListItemTitle(
                text = manga.title,
                bookmarked = chapter.bookmarked,
                fontWeight = FontWeight.SemiBold,
                textColor = textColor,
            )
            MangaListItemSubtitle(
                text = chapter.name,
                textColor = textColor,
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
