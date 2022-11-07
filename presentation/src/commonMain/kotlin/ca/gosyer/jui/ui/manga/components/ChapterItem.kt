/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.manga.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ca.gosyer.jui.i18n.MR
import ca.gosyer.jui.ui.base.chapter.ChapterDownloadIcon
import ca.gosyer.jui.ui.base.chapter.ChapterDownloadItem
import ca.gosyer.jui.uicore.components.selectedBackground
import ca.gosyer.jui.uicore.resources.stringResource
import kotlinx.datetime.Instant

expect fun Modifier.chapterItemModifier(
    onClick: () -> Unit,
    markRead: (() -> Unit)?,
    markUnread: (() -> Unit)?,
    bookmarkChapter: (() -> Unit)?,
    unBookmarkChapter: (() -> Unit)?,
    markPreviousAsRead: () -> Unit,
    onSelectChapter: (() -> Unit)?,
    onUnselectChapter: (() -> Unit)?
): Modifier

@Composable
fun ChapterItem(
    chapterDownload: ChapterDownloadItem,
    format: (Instant) -> String,
    onClick: (Int) -> Unit,
    markRead: (Int) -> Unit,
    markUnread: (Int) -> Unit,
    bookmarkChapter: (Int) -> Unit,
    unBookmarkChapter: (Int) -> Unit,
    markPreviousAsRead: (Int) -> Unit,
    onClickDownload: (Int) -> Unit,
    onClickStopDownload: (Int) -> Unit,
    onClickDeleteChapter: (Int) -> Unit,
    onSelectChapter: (Int) -> Unit,
    onUnselectChapter: (Int) -> Unit
) {
    val chapter = chapterDownload.chapter
    val isSelected by chapterDownload.isSelected.collectAsState()
    BoxWithConstraints(
        Modifier
            .fillMaxWidth()
            .height(70.dp)
            .selectedBackground(isSelected)
            .chapterItemModifier(
                onClick = { onClick(chapter.index) },
                markRead = { markRead(chapter.index) }.takeUnless { chapter.read },
                markUnread = { markUnread(chapter.index) }.takeIf { chapter.read },
                bookmarkChapter = { bookmarkChapter(chapter.index) }.takeUnless { chapter.bookmarked },
                unBookmarkChapter = { unBookmarkChapter(chapter.index) }.takeIf { chapter.bookmarked },
                markPreviousAsRead = { markPreviousAsRead(chapter.index) },
                onSelectChapter = { onSelectChapter(chapter.index) }.takeUnless { chapterDownload.isSelected.value },
                onUnselectChapter = { onUnselectChapter(chapter.index) }.takeIf { chapterDownload.isSelected.value }
            )
            .padding(4.dp)
    ) {
        val textColor = if (chapter.bookmarked && !chapter.read) {
            MaterialTheme.colors.primary
        } else {
            MaterialTheme.colors.onSurface
        }

        Row(
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                Modifier.padding(4.dp).width(this@BoxWithConstraints.maxWidth - 60.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    var textHeight by remember { mutableStateOf(0) }
                    if (chapter.bookmarked) {
                        Icon(
                            imageVector = Icons.Filled.Bookmark,
                            contentDescription = stringResource(MR.strings.action_filter_bookmarked),
                            modifier = Modifier
                                .sizeIn(maxHeight = with(LocalDensity.current) { textHeight.toDp() - 2.dp }),
                            tint = MaterialTheme.colors.primary
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                    }
                    Text(
                        chapter.name,
                        maxLines = 1,
                        style = MaterialTheme.typography.h6,
                        color = textColor.copy(
                            alpha = if (chapter.read) ContentAlpha.disabled else ContentAlpha.high
                        ),
                        overflow = TextOverflow.Ellipsis,
                        onTextLayout = {
                            textHeight = it.size.height
                        }
                    )
                }
                val subtitleStr = buildAnnotatedString {
                    if (chapter.uploadDate > 0) {
                        append(format(Instant.fromEpochMilliseconds(chapter.uploadDate)))
                    }
                    if (!chapter.read && chapter.lastPageRead > 0) {
                        if (length > 0) append(" • ")
                        append(
                            AnnotatedString(
                                stringResource(MR.strings.page_progress, (chapter.lastPageRead + 1)),
                                SpanStyle(color = textColor.copy(alpha = ContentAlpha.disabled))
                            )
                        )
                    }
                    if (!chapter.scanlator.isNullOrBlank()) {
                        if (length > 0) append(" • ")
                        append(chapter.scanlator!!)
                    }
                }
                Text(
                    subtitleStr,
                    style = MaterialTheme.typography.body2,
                    color = textColor.copy(
                        alpha = if (chapter.read) ContentAlpha.disabled else ContentAlpha.medium
                    )
                )
            }

            ChapterDownloadIcon(
                chapterDownload,
                { onClickDownload(it.index) },
                { onClickStopDownload(it.index) },
                { onClickDeleteChapter(it.index) }
            )
        }
    }
}
