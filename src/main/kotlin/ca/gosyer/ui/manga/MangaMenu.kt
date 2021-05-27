/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.manga

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ca.gosyer.BuildConfig
import ca.gosyer.data.models.Chapter
import ca.gosyer.data.models.Manga
import ca.gosyer.ui.base.components.KtorImage
import ca.gosyer.ui.base.components.LoadingScreen
import ca.gosyer.ui.base.components.Toolbar
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
            Row {
                Button(onClick = vm::toggleFavorite) {
                    Text(if (manga?.inLibrary == true) "UnFavorite" else "Favorite")
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
                            markPreviousAsRead = vm::markPreviousRead
                        )
                    }
                }
                VerticalScrollbar(
                    modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
                    adapter = rememberScrollbarAdapter(
                        scrollState = state,
                        itemCount = chapters.size + 1,
                        averageItemSize = 70.dp
                    )
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

sealed class MangaMenu {
    data class MangaMenuManga(val manga: Manga) : MangaMenu()

    data class MangaMenuChapter(val chapter: Chapter) : MangaMenu()
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
    chapter: Chapter,
    format: (Instant) -> String,
    onClick: (Int) -> Unit,
    toggleRead: (Int) -> Unit,
    toggleBookmarked: (Int) -> Unit,
    markPreviousAsRead: (Int) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth().height(70.dp).padding(4.dp),
        elevation = 1.dp
    ) {
        Column(
            Modifier.padding(4.dp)
                .combinedMouseClickable(
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
                )
        ) {
            Text(
                chapter.name, fontSize = 20.sp, maxLines = 1,
                color = if (!chapter.read) {
                    LocalContentColor.current
                } else {
                    LocalContentColor.current.copy(alpha = ContentAlpha.disabled)
                }
            )
            val description = mutableListOf<String>()
            if (chapter.uploadDate != 0L) {
                description += format(Instant.ofEpochMilli(chapter.uploadDate))
            }
            if (!chapter.scanlator.isNullOrEmpty()) {
                description += chapter.scanlator
            }
            if (description.isNotEmpty()) {
                Spacer(Modifier.height(2.dp))
                Text(
                    description.joinToString(" - "), maxLines = 1,
                    color = if (!chapter.read) {
                        LocalContentColor.current
                    } else {
                        LocalContentColor.current.copy(alpha = ContentAlpha.disabled)
                    }
                )
            }
        }
    }
}
