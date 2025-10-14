/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.base.chapter

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
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ca.gosyer.jui.domain.chapter.interactor.DeleteChapterDownload
import ca.gosyer.jui.domain.chapter.model.Chapter
import ca.gosyer.jui.domain.download.interactor.StopChapterDownload
import ca.gosyer.jui.domain.download.model.DownloadQueueItem
import ca.gosyer.jui.domain.download.model.DownloadState
import ca.gosyer.jui.domain.manga.model.Manga
import ca.gosyer.jui.i18n.MR
import ca.gosyer.jui.uicore.components.DropdownIconButton
import ca.gosyer.jui.uicore.resources.stringResource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

@Stable
data class ChapterDownloadItem(
    val manga: Manga?,
    val chapter: Chapter,
) {
    private val _isSelected = MutableStateFlow(false)
    val isSelected = _isSelected.asStateFlow()

    private val _downloadState: MutableStateFlow<ChapterDownloadState> = MutableStateFlow(
        when (chapter.downloaded) {
            true -> ChapterDownloadState.Downloaded
            false -> ChapterDownloadState.NotDownloaded
        },
    )
    val downloadState = _downloadState.asStateFlow()

    private val _downloadChapterFlow: MutableStateFlow<DownloadQueueItem?> = MutableStateFlow(null)
    val downloadChapterFlow = _downloadChapterFlow.asStateFlow()

    fun updateFrom(downloadingChapters: List<DownloadQueueItem>) {
        val downloadingChapter = downloadingChapters.find {
            it.chapter.id == chapter.id
        }
        if (downloadingChapter != null && downloadState.value != ChapterDownloadState.Downloading) {
            _downloadState.value = ChapterDownloadState.Downloading
        }
        if (downloadState.value == ChapterDownloadState.Downloading && downloadingChapter == null) {
            _downloadState.value = ChapterDownloadState.Downloaded
        }
        _downloadChapterFlow.value = downloadingChapter
    }

    suspend fun deleteDownload(deleteChapterDownload: DeleteChapterDownload) {
        deleteChapterDownload.await(chapter)
        _downloadState.value = ChapterDownloadState.NotDownloaded
    }

    suspend fun stopDownloading(stopChapterDownload: StopChapterDownload) {
        stopChapterDownload.await(chapter.id)
        _downloadState.value = ChapterDownloadState.NotDownloaded
    }

    fun setNotDownloaded() {
        _downloadState.value = ChapterDownloadState.NotDownloaded
    }

    fun isSelected(selectedItems: List<Long>): Boolean = (chapter.id in selectedItems).also { _isSelected.value = it }
}

enum class ChapterDownloadState {
    NotDownloaded,
    Downloading,
    Downloaded,
}

@Composable
fun ChapterDownloadIcon(
    chapter: ChapterDownloadItem,
    onClickDownload: (Chapter) -> Unit,
    onClickStop: (Chapter) -> Unit,
    onClickDelete: (Chapter) -> Unit,
) {
    val downloadChapter by chapter.downloadChapterFlow.collectAsState()
    val downloadState by chapter.downloadState.collectAsState()

    when (downloadState) {
        ChapterDownloadState.Downloaded -> {
            DownloadedIconButton(
                chapter.chapter.mangaId to chapter.chapter.index,
                onClick = { onClickDelete(chapter.chapter) },
            )
        }

        ChapterDownloadState.Downloading -> {
            DownloadingIconButton(
                downloadChapter,
                onClick = { onClickStop(chapter.chapter) },
            )
        }

        ChapterDownloadState.NotDownloaded -> {
            DownloadIconButton(onClick = { onClickDownload(chapter.chapter) })
        }
    }
}

@Composable
private fun DownloadIconButton(onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.fillMaxHeight(),
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
                LocalContentColor.current.copy(alpha = ContentAlpha.disabled),
            )
        }
    }
}

@Composable
private fun DownloadingIconButton(
    downloadChapter: DownloadQueueItem?,
    onClick: () -> Unit,
) {
    DropdownIconButton(
        downloadChapter?.chapter?.id,
        {
            DropdownMenuItem(onClick = onClick) {
                Text(stringResource(MR.strings.action_cancel))
            }
        },
    ) {
        when (downloadChapter?.state) {
            null, DownloadState.QUEUED -> CircularProgressIndicator(
                Modifier
                    .size(26.dp)
                    .padding(2.dp),
                LocalContentColor.current.copy(alpha = ContentAlpha.disabled),
                2.dp,
            )

            DownloadState.DOWNLOADING -> if (downloadChapter.progress != 0.0F) {
                val animatedProgress by animateFloatAsState(
                    targetValue = downloadChapter.progress,
                    animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec,
                )
                CircularProgressIndicator(
                    animatedProgress,
                    Modifier
                        .size(26.dp)
                        .padding(2.dp),
                    LocalContentColor.current.copy(alpha = ContentAlpha.disabled),
                    2.dp,
                )
                Icon(
                    Icons.Rounded.ArrowDownward,
                    null,
                    Modifier
                        .size(22.dp)
                        .padding(2.dp),
                    LocalContentColor.current.copy(alpha = ContentAlpha.disabled),
                )
            } else {
                CircularProgressIndicator(
                    Modifier
                        .size(26.dp)
                        .padding(2.dp),
                    LocalContentColor.current.copy(alpha = ContentAlpha.disabled),
                    2.dp,
                )
            }

            DownloadState.ERROR -> Surface(shape = CircleShape, color = LocalContentColor.current) {
                Icon(
                    Icons.Rounded.Error,
                    null,
                    Modifier
                        .size(22.dp)
                        .padding(2.dp),
                    Color.Red,
                )
            }

            DownloadState.FINISHED -> Surface(shape = CircleShape, color = LocalContentColor.current) {
                Icon(
                    Icons.Rounded.Check,
                    null,
                    Modifier
                        .size(22.dp)
                        .padding(2.dp),
                    MaterialTheme.colors.surface,
                )
            }
        }
    }
}

@Composable
private fun DownloadedIconButton(
    chapter: Pair<Long, Int?>,
    onClick: () -> Unit,
) {
    DropdownIconButton(
        chapter,
        {
            DropdownMenuItem(onClick = onClick) {
                Text(stringResource(MR.strings.action_delete))
            }
        },
    ) {
        Surface(shape = CircleShape, color = LocalContentColor.current) {
            Icon(
                Icons.Rounded.Check,
                null,
                Modifier
                    .size(22.dp)
                    .padding(2.dp),
                MaterialTheme.colors.surface,
            )
        }
    }
}
