/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.downloads

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProgressIndicatorDefaults
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ClearAll
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ca.gosyer.build.BuildConfig
import ca.gosyer.data.download.model.DownloadChapter
import ca.gosyer.data.download.model.DownloaderStatus
import ca.gosyer.data.models.Chapter
import ca.gosyer.ui.base.components.ActionIcon
import ca.gosyer.ui.base.components.DropdownIconButton
import ca.gosyer.ui.base.components.MangaListItem
import ca.gosyer.ui.base.components.MangaListItemColumn
import ca.gosyer.ui.base.components.MangaListItemImage
import ca.gosyer.ui.base.components.MangaListItemSubtitle
import ca.gosyer.ui.base.components.MangaListItemTitle
import ca.gosyer.ui.base.components.Toolbar
import ca.gosyer.ui.base.components.mangaAspectRatio
import ca.gosyer.ui.base.resources.stringResource
import ca.gosyer.ui.base.vm.viewModel
import ca.gosyer.ui.manga.openMangaMenu
import ca.gosyer.util.compose.ThemedWindow
import ca.gosyer.util.lang.launchApplication
import io.kamel.image.lazyPainterResource
import kotlinx.coroutines.DelicateCoroutinesApi

@OptIn(DelicateCoroutinesApi::class)
fun openDownloadsMenu() {
    launchApplication {
        ThemedWindow(::exitApplication, title = BuildConfig.NAME) {
            Surface {
                DownloadsMenu(::openMangaMenu)
            }
        }
    }
}

@Composable
fun DownloadsMenu(onMangaClick: (Long) -> Unit) {
    val vm = viewModel<DownloadsMenuViewModel>()
    val downloadQueue by vm.downloadQueue.collectAsState()

    Column {
        Toolbar(
            stringResource("location_downloads"),
            closable = false,
            actions = {
                val downloadStatus by vm.downloaderStatus.collectAsState()
                if (downloadStatus == DownloaderStatus.Started) {
                    ActionIcon(onClick = vm::pause, stringResource("action_pause"), Icons.Rounded.Pause)
                } else {
                    ActionIcon(onClick = vm::start, stringResource("action_continue"), Icons.Rounded.PlayArrow)
                }
                ActionIcon(onClick = vm::clear, stringResource("action_clear_queue"), Icons.Rounded.ClearAll)
            }
        )
        Box {
            val state = rememberLazyListState()
            LazyColumn(Modifier.fillMaxSize(), state) {
                items(downloadQueue) {
                    DownloadsItem(
                        it,
                        { onMangaClick(it.mangaId) },
                        vm::stopDownload,
                        vm::moveToBottom
                    )
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

@Composable
fun DownloadsItem(
    item: DownloadChapter,
    onClickCover: () -> Unit,
    onClickCancel: (Chapter) -> Unit,
    onClickMoveToBottom: (Chapter) -> Unit
) {
    MangaListItem(
        modifier = Modifier
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
            cover = lazyPainterResource(item.manga, filterQuality = FilterQuality.Medium),
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
                " - " + "${(item.chapter.pageCount * item.progress).toInt()}/${item.chapter.pageCount}"
            } else ""
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
                    Text(stringResource("action_cancel"))
                }
                DropdownMenuItem(onClick = { onClickMoveToBottom(item.chapter) }) {
                    Text(stringResource("action_move_to_bottom"))
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
