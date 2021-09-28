/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.base.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ContentAlpha
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProgressIndicatorDefaults
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Error
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ca.gosyer.data.download.model.DownloadChapter
import ca.gosyer.data.download.model.DownloadState
import ca.gosyer.data.models.Chapter
import ca.gosyer.data.models.Manga
import ca.gosyer.data.server.interactions.ChapterInteractionHandler
import ca.gosyer.ui.base.resources.stringResource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ChapterDownloadItem(
    val manga: Manga?,
    val chapter: Chapter,
    private val _downloadState: MutableStateFlow<ChapterDownloadState> = MutableStateFlow(
        if (chapter.downloaded) {
            ChapterDownloadState.Downloaded
        } else {
            ChapterDownloadState.NotDownloaded
        }
    ),
    private val _downloadChapterFlow: MutableStateFlow<DownloadChapter?> = MutableStateFlow(null)
) {
    val downloadState = _downloadState.asStateFlow()
    val downloadChapterFlow = _downloadChapterFlow.asStateFlow()

    fun updateFrom(downloadingChapters: List<DownloadChapter>) {
        val downloadingChapter = downloadingChapters.find {
            it.chapterIndex == chapter.index
        }
        if (downloadingChapter != null && downloadState.value != ChapterDownloadState.Downloading) {
            _downloadState.value = ChapterDownloadState.Downloading
        }
        if (downloadState.value == ChapterDownloadState.Downloading && downloadingChapter == null) {
            _downloadState.value = ChapterDownloadState.Downloaded
        }
        _downloadChapterFlow.value = downloadingChapter
    }

    suspend fun deleteDownload(chapterHandler: ChapterInteractionHandler) {
        chapterHandler.deleteChapterDownload(chapter)
        _downloadState.value = ChapterDownloadState.NotDownloaded
    }
}

enum class ChapterDownloadState {
    NotDownloaded,
    Downloading,
    Downloaded
}

@Composable
fun ChapterDownloadIcon(
    chapter: ChapterDownloadItem,
    downloadAChapter: (Chapter) -> Unit,
    deleteDownload: (Chapter) -> Unit
) {
    val downloadChapter by chapter.downloadChapterFlow.collectAsState()
    val downloadState by chapter.downloadState.collectAsState()

    when (downloadState) {
        ChapterDownloadState.Downloaded -> {
            DownloadedIconButton(
                chapter.chapter.mangaId to chapter.chapter.index,
                onClick = { deleteDownload(chapter.chapter) }
            )
        }
        ChapterDownloadState.Downloading -> {
            DownloadingIconButton(
                downloadChapter,
                onClick = { deleteDownload(chapter.chapter) }
            )
        }
        ChapterDownloadState.NotDownloaded -> {
            DownloadIconButton(onClick = { downloadAChapter(chapter.chapter) })
        }
    }
}

@Composable
private fun DownloadIconButton(onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.fillMaxHeight()
    ) {
        Surface(
            shape = CircleShape,
            border = BorderStroke(2.dp, LocalContentColor.current.copy(alpha = ContentAlpha.disabled)),
        ) {
            Icon(
                Icons.Rounded.ArrowDownward,
                null,
                Modifier
                    .size(22.dp)
                    .padding(2.dp),
                LocalContentColor.current.copy(alpha = ContentAlpha.disabled)
            )
        }
    }
}

@Composable
private fun DownloadingIconButton(downloadChapter: DownloadChapter?, onClick: () -> Unit) {
    DropdownIconButton(
        downloadChapter?.mangaId to downloadChapter?.chapterIndex,
        {
            DropdownMenuItem(onClick = onClick) {
                Text(stringResource("action_cancel"))
            }
        }
    ) {
        when (downloadChapter?.state) {
            null, DownloadState.Queued -> CircularProgressIndicator(
                Modifier
                    .size(26.dp)
                    .padding(2.dp),
                LocalContentColor.current.copy(alpha = ContentAlpha.disabled),
                2.dp
            )
            DownloadState.Downloading -> if (downloadChapter.progress != 0.0F) {
                val animatedProgress by animateFloatAsState(
                    targetValue = downloadChapter.progress,
                    animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec
                )
                CircularProgressIndicator(
                    animatedProgress,
                    Modifier
                        .size(26.dp)
                        .padding(2.dp),
                    LocalContentColor.current.copy(alpha = ContentAlpha.disabled),
                    2.dp
                )
                Icon(
                    Icons.Rounded.ArrowDownward,
                    null,
                    Modifier
                        .size(22.dp)
                        .padding(2.dp),
                    LocalContentColor.current.copy(alpha = ContentAlpha.disabled)
                )
            } else {
                CircularProgressIndicator(
                    Modifier
                        .size(26.dp)
                        .padding(2.dp),
                    LocalContentColor.current.copy(alpha = ContentAlpha.disabled),
                    2.dp
                )
            }
            DownloadState.Error -> Surface(shape = CircleShape, color = LocalContentColor.current) {
                Icon(
                    Icons.Rounded.Error,
                    null,
                    Modifier
                        .size(22.dp)
                        .padding(2.dp),
                    Color.Red
                )
            }
            DownloadState.Finished -> Surface(shape = CircleShape, color = LocalContentColor.current) {
                Icon(
                    Icons.Rounded.Check,
                    null,
                    Modifier
                        .size(22.dp)
                        .padding(2.dp),
                    MaterialTheme.colors.surface
                )
            }
        }
    }
}

@Composable
private fun DownloadedIconButton(chapter: Pair<Long, Int?>, onClick: () -> Unit) {
    DropdownIconButton(
        chapter,
        {
            DropdownMenuItem(onClick = onClick) {
                Text(stringResource("action_delete"))
            }
        }
    ) {
        Surface(shape = CircleShape, color = LocalContentColor.current) {
            Icon(
                Icons.Rounded.Check,
                null,
                Modifier
                    .size(22.dp)
                    .padding(2.dp),
                MaterialTheme.colors.surface
            )
        }
    }
}
