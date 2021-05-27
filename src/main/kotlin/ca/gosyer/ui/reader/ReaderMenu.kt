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
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import ca.gosyer.common.di.AppScope
import ca.gosyer.data.reader.model.Direction
import ca.gosyer.data.ui.UiPreferences
import ca.gosyer.data.ui.model.WindowSettings
import ca.gosyer.ui.base.KeyboardShortcut
import ca.gosyer.ui.base.components.ErrorScreen
import ca.gosyer.ui.base.components.LoadingScreen
import ca.gosyer.ui.base.components.mangaAspectRatio
import ca.gosyer.ui.base.theme.AppTheme
import ca.gosyer.ui.base.vm.viewModel
import ca.gosyer.ui.reader.model.MoveTo
import ca.gosyer.ui.reader.model.ReaderChapter
import ca.gosyer.ui.reader.model.ReaderPage
import ca.gosyer.ui.reader.viewer.ContinuousReader
import ca.gosyer.ui.reader.viewer.PagerReader
import ca.gosyer.util.lang.launchUI
import kotlinx.coroutines.DelicateCoroutinesApi

@OptIn(DelicateCoroutinesApi::class)
fun openReaderMenu(chapterIndex: Int, mangaId: Long) {
    val windowSettings = AppScope.getInstance<UiPreferences>()
        .readerWindow()
    val (
        offset,
        size,
        maximized
    ) = windowSettings.get().get()

    launchUI {
        val window = AppWindow(
            "TachideskJUI - Reader",
            size = size,
            location = offset,
            centered = offset == IntOffset.Zero
        )

        if (maximized) {
            window.maximize()
        }

        val setHotkeys: (List<KeyboardShortcut>) -> Unit = { shortcuts ->
            shortcuts.forEach {
                window.keyboard.setShortcut(it.key) { it.shortcut(window) }
            }
        }

        window.events.onClose = {
            windowSettings.set(
                WindowSettings(
                    window.x,
                    window.y,
                    window.width,
                    window.height,
                    window.isMaximized
                )
            )
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
    LaunchedEffect(Unit) {
        setHotkeys(
            listOf(
                KeyboardShortcut(Key.W) {
                    vm.moveDirection(MoveTo.Previous)
                },
                KeyboardShortcut(Key.DirectionUp) {
                    vm.moveDirection(MoveTo.Previous)
                },
                KeyboardShortcut(Key.S) {
                    vm.moveDirection(MoveTo.Next)
                },
                KeyboardShortcut(Key.DirectionDown) {
                    vm.moveDirection(MoveTo.Next)
                },
                KeyboardShortcut(Key.A) {
                    vm.moveDirection(
                        when (direction) {
                            Direction.Left -> MoveTo.Next
                            else -> MoveTo.Previous
                        }
                    )
                },
                KeyboardShortcut(Key.DirectionLeft) {
                    vm.moveDirection(
                        when (direction) {
                            Direction.Left -> MoveTo.Next
                            else -> MoveTo.Previous
                        }
                    )
                },
                KeyboardShortcut(Key.D) {
                    vm.moveDirection(
                        when (direction) {
                            Direction.Left -> MoveTo.Previous
                            else -> MoveTo.Next
                        }
                    )
                },
                KeyboardShortcut(Key.DirectionRight) {
                    vm.moveDirection(
                        when (direction) {
                            Direction.Left -> MoveTo.Previous
                            else -> MoveTo.Next
                        }
                    )
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
                        ContinuousReader(
                            pages,
                            previousChapter,
                            chapter,
                            nextChapter,
                            pageModifier,
                            vm.pageEmitter,
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
                            vm.pageEmitter,
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
fun ChapterSeperator(
    previousChapter: ReaderChapter?,
    nextChapter: ReaderChapter?
) {
    Box(Modifier.fillMaxWidth().height(350.dp), contentAlignment = Alignment.Center) {
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
