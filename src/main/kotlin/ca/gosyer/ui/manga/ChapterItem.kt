/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.manga

import androidx.compose.foundation.ContextMenuItem
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.Card
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ca.gosyer.ui.base.components.ChapterDownloadIcon
import ca.gosyer.ui.base.components.ChapterDownloadItem
import ca.gosyer.ui.base.components.contextMenuClickable
import ca.gosyer.ui.base.resources.stringResource
import java.time.Instant

@Composable
fun ChapterItem(
    chapterDownload: ChapterDownloadItem,
    format: (Instant) -> String,
    onClick: (Int) -> Unit,
    toggleRead: (Int) -> Unit,
    toggleBookmarked: (Int) -> Unit,
    markPreviousAsRead: (Int) -> Unit,
    onClickDownload: (Int) -> Unit,
    onClickStopDownload: (Int) -> Unit,
    onClickDeleteChapter: (Int) -> Unit
) {
    val chapter = chapterDownload.chapter
    Card(
        modifier = Modifier.fillMaxWidth().height(70.dp).padding(4.dp),
        elevation = 1.dp,
        shape = RoundedCornerShape(4.dp)
    ) {
        BoxWithConstraints(
            Modifier.contextMenuClickable(
                {
                    listOf(
                        ContextMenuItem("Toggle read") { toggleRead(chapter.index) },
                        ContextMenuItem("Mark previous as read") { markPreviousAsRead(chapter.index) },
                        ContextMenuItem("Toggle bookmarked") { toggleBookmarked(chapter.index) }
                    )
                },
                onClick = { onClick(chapter.index) }
            )
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    Modifier.padding(4.dp).width(this@BoxWithConstraints.maxWidth - 60.dp)
                ) {
                    SelectionContainer {
                        Text(
                            chapter.name,
                            maxLines = 1,
                            style = MaterialTheme.typography.h6,
                            color = LocalContentColor.current.copy(
                                alpha = if (chapter.read) ContentAlpha.disabled else ContentAlpha.high
                            ),
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    val subtitleStr = buildAnnotatedString {
                        if (chapter.uploadDate > 0) {
                            append(format(Instant.ofEpochMilli(chapter.uploadDate)))
                        }
                        if (!chapter.read && chapter.lastPageRead > 0) {
                            if (length > 0) append(" • ")
                            append(
                                AnnotatedString(
                                    stringResource("page_progress", (chapter.lastPageRead + 1)),
                                    SpanStyle(color = LocalContentColor.current.copy(alpha = ContentAlpha.disabled))
                                )
                            )
                        }
                        if (!chapter.scanlator.isNullOrBlank()) {
                            if (length > 0) append(" • ")
                            append(chapter.scanlator)
                        }
                    }
                    SelectionContainer {
                        Text(
                            subtitleStr,
                            style = MaterialTheme.typography.body2,
                            color = LocalContentColor.current.copy(
                                alpha = if (chapter.read) ContentAlpha.disabled else ContentAlpha.medium
                            )
                        )
                    }
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
}
