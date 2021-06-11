/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.manga

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.forEachGesture
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ContentAlpha
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ca.gosyer.BuildConfig
import ca.gosyer.data.download.model.DownloadChapter
import ca.gosyer.data.download.model.DownloadState
import ca.gosyer.data.models.Manga
import ca.gosyer.ui.base.components.KtorImage
import ca.gosyer.ui.base.components.LoadingScreen
import ca.gosyer.ui.base.components.Toolbar
import ca.gosyer.ui.base.components.awaitEventFirstDown
import ca.gosyer.ui.base.components.combinedMouseClickable
import ca.gosyer.ui.base.components.mangaAspectRatio
import ca.gosyer.ui.base.vm.viewModel
import ca.gosyer.ui.main.Route
import ca.gosyer.ui.reader.openReaderMenu
import ca.gosyer.util.compose.ThemedWindow
import ca.gosyer.util.compose.contextMenu
import com.github.zsoltk.compose.router.BackStack
import java.time.Instant

fun openMangaMenu(mangaId: Long) {
    ThemedWindow(BuildConfig.NAME) {
        MangaMenu(mangaId)
    }
}

@Composable
fun MangaMenu(mangaId: Long, backStack: BackStack<Route>? = null) {
    val vm = viewModel<MangaMenuViewModel> {
        MangaMenuViewModel.Params(mangaId)
    }
    val manga by vm.manga.collectAsState()
    val chapters by vm.chapters.collectAsState()
    val isLoading by vm.isLoading.collectAsState()
    val serverUrl by vm.serverUrl.collectAsState()
    val dateTimeFormatter by vm.dateTimeFormatter.collectAsState()

    Column(Modifier.background(MaterialTheme.colors.background)) {
        Toolbar("Manga", backStack, backStack != null)

        Surface(Modifier.height(40.dp).fillMaxWidth()) {
            Row(horizontalArrangement = Arrangement.SpaceBetween) {
                Button(onClick = vm::toggleFavorite) {
                    Text(if (manga?.inLibrary == true) "UnFavorite" else "Favorite")
                }
                Button(onClick = vm::refreshManga, enabled = !vm.isRefreshing.collectAsState().value) {
                    Text("Refresh manga")
                }
            }
        }
        manga?.let { manga ->
            Box {
                val state = rememberLazyListState()
                LazyColumn(state = state) {
                    item {
                        MangaItem(manga, serverUrl)
                    }
                    items(chapters) { chapter ->
                        ChapterItem(
                            chapter,
                            dateTimeFormatter::format,
                            onClick = { openReaderMenu(it, manga.id) },
                            toggleRead = vm::toggleRead,
                            toggleBookmarked = vm::toggleBookmarked,
                            markPreviousAsRead = vm::markPreviousRead,
                            downloadAChapter = vm::downloadChapter,
                            deleteDownload = vm::deleteDownload,
                            stopDownload = vm::deleteDownload
                        )
                    }
                }
                VerticalScrollbar(
                    modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
                    adapter = rememberScrollbarAdapter(state)
                )
                if (isLoading) {
                    LoadingScreen()
                }
            }
        }
    }
}

@Composable
fun MangaItem(manga: Manga, serverUrl: String) {
    BoxWithConstraints(Modifier.padding(8.dp)) {
        if (maxWidth > 600.dp) {
            Row {
                Cover(manga, serverUrl)
                Spacer(Modifier.width(16.dp))
                Surface(
                    elevation = 2.dp,
                    modifier = Modifier.defaultMinSize(minHeight = 450.dp).fillMaxWidth()
                ) {
                    MangaInfo(manga)
                }
            }
        } else {
            Column {
                Cover(manga, serverUrl, Modifier.align(Alignment.CenterHorizontally))
                Spacer(Modifier.height(16.dp))
                Surface(elevation = 2.dp) {
                    MangaInfo(manga)
                }
            }
        }
    }
}

@Composable
private fun Cover(manga: Manga, serverUrl: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier then Modifier
            .width(300.dp)
            .aspectRatio(mangaAspectRatio)
            .padding(4.dp),
        elevation = 4.dp,
        shape = RoundedCornerShape(4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            manga.cover(serverUrl)?.let {
                KtorImage(it)
            }
        }
    }
}

@Composable
private fun MangaInfo(manga: Manga, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(manga.title, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        if (!manga.author.isNullOrEmpty()) {
            Text(manga.author, fontSize = 18.sp)
        }
        if (!manga.artist.isNullOrEmpty() && manga.artist != manga.author) {
            Text(manga.artist, fontSize = 18.sp)
        }
        if (!manga.description.isNullOrEmpty()) {
            Text(manga.description)
        }
        if (!manga.genre.isNullOrEmpty()) {
            Text(manga.genre)
        }
    }
}

@Composable
fun ChapterItem(
    viewChapter: MangaMenuViewModel.ViewChapter,
    format: (Instant) -> String,
    onClick: (Int) -> Unit,
    toggleRead: (Int) -> Unit,
    toggleBookmarked: (Int) -> Unit,
    markPreviousAsRead: (Int) -> Unit,
    downloadAChapter: (Int) -> Unit,
    deleteDownload: (Int) -> Unit,
    stopDownload: (Int) -> Unit
) {
    val chapter = viewChapter.chapter
    Card(
        modifier = Modifier.fillMaxWidth().height(70.dp).padding(4.dp),
        elevation = 1.dp,
        shape = RoundedCornerShape(4.dp)
    ) {
        Row(
            Modifier.combinedMouseClickable(
                onClick = {
                    onClick(chapter.index)
                },
                onRightClick = {
                    contextMenu(
                        it
                    ) {
                        menuItem("Toggle read") { toggleRead(chapter.index) }
                        menuItem("Mark previous as read") { markPreviousAsRead(chapter.index) }
                        separator()
                        menuItem("Toggle bookmarked") { toggleBookmarked(chapter.index) }
                    }
                }
            ),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                Modifier.padding(4.dp)
            ) {
                Text(
                    chapter.name,
                    maxLines = 1,
                    style = MaterialTheme.typography.h6,
                    color = LocalContentColor.current.copy(
                        alpha = if (chapter.read) ContentAlpha.disabled else ContentAlpha.high
                    )
                )
                val subtitleStr = buildAnnotatedString {
                    if (chapter.uploadDate > 0) {
                        append(format(Instant.ofEpochMilli(chapter.uploadDate)))
                    }
                    if (!chapter.read && chapter.lastPageRead > 0) {
                        if (length > 0) append(" • ")
                        append(
                            AnnotatedString(
                                "Page " + (chapter.lastPageRead + 1).toString(),
                                SpanStyle(color = LocalContentColor.current.copy(alpha = ContentAlpha.disabled))
                            )
                        )
                    }
                    if (!chapter.scanlator.isNullOrBlank()) {
                        if (length > 0) append(" • ")
                        append(chapter.scanlator)
                    }
                }
                Text(
                    subtitleStr,
                    style = MaterialTheme.typography.body2,
                    color = LocalContentColor.current.copy(
                        alpha = if (chapter.read) ContentAlpha.disabled else ContentAlpha.medium
                    )
                )
            }
            val downloadChapter by viewChapter.downloadChapterFlow.collectAsState()
            val downloadState by viewChapter.downloadState.collectAsState()

            when (downloadState) {
                MangaMenuViewModel.DownloadState.Downloaded -> {
                    DownloadedIconButton(onClick = { deleteDownload(chapter.index) })
                }
                MangaMenuViewModel.DownloadState.Downloading -> {
                    DownloadingIconButton(downloadChapter, onClick = { stopDownload(chapter.index) })
                }
                MangaMenuViewModel.DownloadState.NotDownloaded -> {
                    DownloadIconButton(onClick = { downloadAChapter(chapter.index) })
                }
            }
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
                Icons.Default.ArrowDownward,
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
        {
            DropdownMenuItem(onClick = onClick) {
                Text("Cancel")
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
                CircularProgressIndicator(
                    downloadChapter.progress,
                    Modifier
                        .size(26.dp)
                        .padding(2.dp),
                    LocalContentColor.current.copy(alpha = ContentAlpha.disabled),
                    2.dp
                )
                Icon(
                    Icons.Default.ArrowDownward,
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
                    Icons.Default.Error,
                    null,
                    Modifier
                        .size(22.dp)
                        .padding(2.dp),
                    Color.Red
                )
            }
            DownloadState.Finished -> Surface(shape = CircleShape, color = LocalContentColor.current) {
                Icon(
                    Icons.Default.Check,
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
private fun DownloadedIconButton(onClick: () -> Unit) {
    DropdownIconButton(
        {
            DropdownMenuItem(onClick = onClick) {
                Text("Delete")
            }
        }
    ) {
        Surface(shape = CircleShape, color = LocalContentColor.current) {
            Icon(
                Icons.Default.Check,
                null,
                Modifier
                    .size(22.dp)
                    .padding(2.dp),
                MaterialTheme.colors.surface
            )
        }
    }
}

@Composable
fun DropdownIconButton(
    dropdownItems: @Composable ColumnScope.() -> Unit,
    content: @Composable BoxScope.() -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    var offset by remember { mutableStateOf(DpOffset(0.dp, 0.dp)) }
    DropdownMenu(
        expanded = showMenu,
        onDismissRequest = { showMenu = false },
        offset = offset,
        content = dropdownItems
    )
    Box(
        modifier = Modifier.fillMaxHeight()
            .size(48.dp)
            .clickable(
                remember { MutableInteractionSource() },
                role = Role.Button,
                indication = rememberRipple(bounded = false, radius = 24.dp)
            ) {
                showMenu = true
            }
            .pointerInput(Unit) {
                forEachGesture {
                    awaitPointerEventScope {
                        awaitEventFirstDown().mouseEvent?.let {
                            offset = DpOffset(it.x.dp, it.y.dp)
                        }
                    }
                }
            },
        contentAlignment = Alignment.Center,
        content = content
    )
}
