/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.downloads.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Icon
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProgressIndicatorDefaults
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ClearAll
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ca.gosyer.jui.domain.chapter.model.Chapter
import ca.gosyer.jui.domain.download.model.DownloadChapter
import ca.gosyer.jui.domain.download.model.DownloaderStatus
import ca.gosyer.jui.i18n.MR
import ca.gosyer.jui.ui.base.navigation.ActionItem
import ca.gosyer.jui.ui.base.navigation.Toolbar
import ca.gosyer.jui.ui.main.components.bottomNav
import ca.gosyer.jui.uicore.components.DropdownIconButton
import ca.gosyer.jui.uicore.components.DropdownMenuItem
import ca.gosyer.jui.uicore.components.MangaListItem
import ca.gosyer.jui.uicore.components.MangaListItemColumn
import ca.gosyer.jui.uicore.components.MangaListItemImage
import ca.gosyer.jui.uicore.components.MangaListItemSubtitle
import ca.gosyer.jui.uicore.components.MangaListItemTitle
import ca.gosyer.jui.uicore.components.VerticalScrollbar
import ca.gosyer.jui.uicore.components.mangaAspectRatio
import ca.gosyer.jui.uicore.components.rememberScrollbarAdapter
import ca.gosyer.jui.uicore.components.scrollbarPadding
import ca.gosyer.jui.uicore.insets.navigationBars
import ca.gosyer.jui.uicore.insets.statusBars
import ca.gosyer.jui.uicore.resources.stringResource
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

@Composable
fun DownloadsScreenContent(
    downloadQueue: ImmutableList<DownloadChapter>,
    downloadStatus: DownloaderStatus,
    startDownloading: () -> Unit,
    pauseDownloading: () -> Unit,
    clearQueue: () -> Unit,
    onMangaClick: (Long) -> Unit,
    stopDownload: (Chapter) -> Unit,
    moveDownloadUp: (Chapter) -> Unit,
    moveDownloadDown: (Chapter) -> Unit,
    moveDownloadToTop: (Chapter) -> Unit,
    moveDownloadToBottom: (Chapter) -> Unit
) {
    Scaffold(
        modifier = Modifier.windowInsetsPadding(
            WindowInsets.statusBars.add(
                WindowInsets.navigationBars.only(WindowInsetsSides.Horizontal)
            )
        ),
        topBar = {
            Toolbar(
                stringResource(MR.strings.location_downloads),
                actions = {
                    getActionItems(
                        downloadStatus = downloadStatus,
                        startDownloading = startDownloading,
                        pauseDownloading = pauseDownloading,
                        clearQueue = clearQueue
                    )
                }
            )
        }
    ) { paddingValues ->
        Box(Modifier.padding(paddingValues)) {
            val state = rememberLazyListState()
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = state,
                contentPadding = WindowInsets.bottomNav.add(
                    WindowInsets.navigationBars.only(
                        WindowInsetsSides.Bottom
                    )
                ).asPaddingValues()
            ) {
                items(downloadQueue, key = { "${it.mangaId}-${it.chapterIndex}" }) {
                    DownloadsItem(
                        modifier = Modifier.animateItemPlacement(),
                        item = it,
                        onClickCover = { onMangaClick(it.mangaId) },
                        onClickCancel = stopDownload,
                        onClickMoveUp = moveDownloadUp,
                        onClickMoveDown = moveDownloadDown,
                        onClickMoveToTop = moveDownloadToTop,
                        onClickMoveToBottom = moveDownloadToBottom
                    )
                }
            }
            VerticalScrollbar(
                rememberScrollbarAdapter(state),
                Modifier.align(Alignment.CenterEnd)
                    .fillMaxHeight()
                    .scrollbarPadding()
                    .windowInsetsPadding(
                        WindowInsets.bottomNav.add(
                            WindowInsets.navigationBars.only(
                                WindowInsetsSides.Bottom
                            )
                        )
                    )
            )
        }
    }
}

@Composable
fun DownloadsItem(
    modifier: Modifier = Modifier,
    item: DownloadChapter,
    onClickCover: () -> Unit,
    onClickCancel: (Chapter) -> Unit,
    onClickMoveUp: (Chapter) -> Unit,
    onClickMoveDown: (Chapter) -> Unit,
    onClickMoveToTop: (Chapter) -> Unit,
    onClickMoveToBottom: (Chapter) -> Unit
) {
    MangaListItem(
        modifier = modifier
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
            data = item.manga,
            contentDescription = item.manga.title
        )
        MangaListItemColumn(
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp)
        ) {
            MangaListItemTitle(
                text = item.manga.title,
                fontWeight = FontWeight.SemiBold
            )
            val progress = if (item.chapter.pageCount != null && item.chapter.pageCount != -1) {
                " - " + "${(item.chapter.pageCount!! * item.progress).toInt()}/${item.chapter.pageCount}"
            } else {
                ""
            }
            MangaListItemSubtitle(
                text = item.chapter.name + progress
            )
            Spacer(Modifier.height(4.dp))
            val animatedProgress by animateFloatAsState(
                targetValue = item.progress,
                animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec
            )
            LinearProgressIndicator(
                animatedProgress,
                Modifier.fillMaxWidth()
                    .padding(bottom = 8.dp)
            )
        }
        DropdownIconButton(
            item.mangaId to item.chapterIndex,
            {
                DropdownMenuItem(onClick = { onClickCancel(item.chapter) }) {
                    Text(stringResource(MR.strings.action_cancel))
                }
                DropdownMenuItem(onClick = { onClickMoveToTop(item.chapter) }) {
                    Text(stringResource(MR.strings.action_move_to_top))
                }
                DropdownMenuItem(onClick = { onClickMoveUp(item.chapter) }) {
                    Text(stringResource(MR.strings.action_move_up))
                }
                DropdownMenuItem(onClick = { onClickMoveDown(item.chapter) }) {
                    Text(stringResource(MR.strings.action_move_down))
                }
                DropdownMenuItem(onClick = { onClickMoveToBottom(item.chapter) }) {
                    Text(stringResource(MR.strings.action_move_to_bottom))
                }
            }
        ) {
            Icon(
                Icons.Rounded.MoreVert,
                null
            )
        }
        Spacer(Modifier.width(16.dp))
    }
}

@Stable
@Composable
private fun getActionItems(
    downloadStatus: DownloaderStatus,
    startDownloading: () -> Unit,
    pauseDownloading: () -> Unit,
    clearQueue: () -> Unit
): ImmutableList<ActionItem> {
    return listOf(
        if (downloadStatus == DownloaderStatus.Started) {
            ActionItem(
                stringResource(MR.strings.action_pause),
                Icons.Rounded.Pause,
                doAction = pauseDownloading
            )
        } else {
            ActionItem(
                stringResource(MR.strings.action_continue),
                Icons.Rounded.PlayArrow,
                doAction = startDownloading
            )
        },
        ActionItem(stringResource(MR.strings.action_clear_queue), Icons.Rounded.ClearAll, doAction = clearQueue)
    ).toImmutableList()
}
