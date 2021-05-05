/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.reader

import androidx.compose.desktop.AppWindow
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeysSet
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import ca.gosyer.data.reader.model.Direction
import ca.gosyer.ui.base.KeyboardShortcut
import ca.gosyer.ui.base.components.ErrorScreen
import ca.gosyer.ui.base.components.LoadingScreen
import ca.gosyer.ui.base.components.mangaAspectRatio
import ca.gosyer.ui.base.theme.AppTheme
import ca.gosyer.ui.base.vm.viewModel
import ca.gosyer.ui.reader.model.ReaderChapter
import ca.gosyer.ui.reader.model.ReaderPage
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerScope
import com.google.accompanist.pager.VerticalPager
import com.google.accompanist.pager.rememberPagerState
import javax.swing.SwingUtilities

fun openReaderMenu(chapterIndex: Int, mangaId: Long) {
    SwingUtilities.invokeLater {
        val window = AppWindow(
            "TachideskJUI - Reader"
        )

        val setHotkeys: (List<KeyboardShortcut>) -> Unit = { shortcuts ->
            shortcuts.forEach {
                window.keyboard.setShortcut(it.key) { it.shortcut(window) }
            }
        }

        window.show {
            AppTheme {
                ReaderMenu(chapterIndex, mangaId, setHotkeys)
            }
        }
    }
}

@Composable
fun ReaderMenu(chapterIndex: Int, mangaId: Long, setHotkeys: (List<KeyboardShortcut>) -> Unit) {
    val vm = viewModel<ReaderMenuViewModel> {
        ReaderMenuViewModel.Params(chapterIndex, mangaId)
    }

    val state by vm.state.collectAsState()
    val previousChapter by vm.previousChapter.collectAsState()
    val chapter by vm.chapter.collectAsState()
    val nextChapter by vm.nextChapter.collectAsState()
    val pages by vm.pages.collectAsState()
    val continuous by vm.readerModeSettings.continuous.collectAsState()
    val direction by vm.readerModeSettings.direction.collectAsState()
    val padding by vm.readerModeSettings.padding.collectAsState()
    val currentPage by vm.currentPage.collectAsState()
    remember {
        setHotkeys(
            listOf(
                KeyboardShortcut(KeysSet(setOf(Key.W, Key.DirectionUp))) {
                    vm.progress(currentPage + 1)
                },
                KeyboardShortcut(KeysSet(setOf(Key.S, Key.DirectionDown))) {
                    vm.progress(currentPage - 1)
                }
            )
        )
    }

    Surface {
        if (state is ReaderChapter.State.Loaded && chapter != null) {
            chapter?.let { chapter ->
                val pageModifier = Modifier.fillMaxWidth().aspectRatio(mangaAspectRatio)
                if (pages.isNotEmpty()) {
                    if (continuous) {
                        ContinuesReader(
                            pages,
                            pageModifier,
                            vm::retry,
                            vm::progress
                        )
                    } else {
                        PagerReader(
                            direction,
                            currentPage,
                            pages,
                            previousChapter,
                            chapter,
                            nextChapter,
                            pageModifier,
                            vm::retry,
                            vm::progress
                        )
                    }
                } else {
                    ErrorScreen("No pages found")
                }
            }
        } else {
            LoadingScreen(
                state is ReaderChapter.State.Wait || state is ReaderChapter.State.Loading,
                errorMessage = (state as? ReaderChapter.State.Error)?.error?.message
            )
        }
    }
}

@Composable
fun ReaderImage(
    imageIndex: Int,
    drawable: ImageBitmap?,
    status: ReaderPage.Status,
    error: String?,
    imageModifier: Modifier = Modifier.fillMaxSize(),
    loadingModifier: Modifier = imageModifier,
    contentScale: ContentScale = ContentScale.Fit,
    retry: (Int) -> Unit
) {
    if (drawable != null) {
        Image(
            drawable,
            modifier = imageModifier,
            contentDescription = null,
            contentScale = contentScale
        )
    } else {
        LoadingScreen(status == ReaderPage.Status.QUEUE, loadingModifier, error) { retry(imageIndex) }
    }
}

@Composable
fun PagerReader(
    direction: Direction,
    currentPage: Int,
    pages: List<ReaderPage>,
    previousChapter: ReaderChapter?,
    currentChapter: ReaderChapter,
    nextChapter: ReaderChapter?,
    pageModifier: Modifier,
    retry: (ReaderPage) -> Unit,
    progress: (Int) -> Unit
) {
    val state = rememberPagerState(pages.size + 1, initialPage = currentPage)

    LaunchedEffect(state.currentPage) {
        if (state.currentPage != currentPage) {
            progress(state.currentPage)
        }
    }

    if (direction == Direction.Down || direction == Direction.Up) {
        VerticalPager(state, reverseLayout = direction == Direction.Up) {
            HandlePager(
                pages,
                it,
                previousChapter,
                currentChapter,
                nextChapter,
                pageModifier,
                retry
            )
        }
    } else {
        HorizontalPager(state, reverseLayout = direction == Direction.Left) {
            HandlePager(
                pages,
                it,
                previousChapter,
                currentChapter,
                nextChapter,
                pageModifier,
                retry
            )
        }
    }
}

@Composable
fun PagerScope.HandlePager(
    pages: List<ReaderPage>,
    page: Int,
    previousChapter: ReaderChapter?,
    currentChapter: ReaderChapter,
    nextChapter: ReaderChapter?,
    pageModifier: Modifier,
    retry: (ReaderPage) -> Unit,
) {
    when (page) {
        0 -> ChapterSeperator(previousChapter, currentChapter)
        pages.size -> ChapterSeperator(currentChapter, nextChapter)
        else -> {
            val image = pages[page - 1]
            ReaderImage(
                image.index,
                image.bitmap.collectAsState().value,
                image.status.collectAsState().value,
                image.error.collectAsState().value,
                loadingModifier = pageModifier,
                retry = { pageIndex ->
                    pages.find { it.index == pageIndex }?.let { retry(it) }
                }
            )
        }
    }
}

@Composable
fun ChapterSeperator(
    previousChapter: ReaderChapter?,
    nextChapter: ReaderChapter?
) {
    Box(contentAlignment = Alignment.Center) {
        Column {
            when {
                previousChapter == null && nextChapter != null -> {
                    Text("There is no previous chapter")
                }
                previousChapter != null && nextChapter != null -> {
                    Text("Previous:\n ${previousChapter.chapter.name}")
                    Spacer(Modifier.height(8.dp))
                    Text("Next:\n ${nextChapter.chapter.name}")
                }
                previousChapter != null && nextChapter == null -> {
                    Text("There is no next chapter")
                }
            }
        }
    }
}

@Composable
fun ContinuesReader(
    pages: List<ReaderPage>,
    pageModifier: Modifier,
    retry: (ReaderPage) -> Unit,
    progress: (Int) -> Unit
) {
    LazyColumn {
        items(pages) { image ->
            LaunchedEffect(image.index) {
                progress(image.index)
            }
            ReaderImage(
                image.index,
                image.bitmap.collectAsState().value,
                image.status.collectAsState().value,
                image.error.collectAsState().value,
                loadingModifier = pageModifier,
                retry = { pageIndex ->
                    pages.find { it.index == pageIndex }?.let { retry(it) }
                }
            )
        }
    }
}
