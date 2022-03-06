/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.reader

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.key
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import ca.gosyer.data.reader.model.Direction
import ca.gosyer.data.reader.model.ImageScale
import ca.gosyer.data.reader.model.NavigationMode
import ca.gosyer.i18n.MR
import ca.gosyer.ui.reader.model.Navigation
import ca.gosyer.ui.reader.model.ReaderChapter
import ca.gosyer.ui.reader.model.ReaderPage
import ca.gosyer.ui.reader.navigation.EdgeNavigation
import ca.gosyer.ui.reader.navigation.KindlishNavigation
import ca.gosyer.ui.reader.navigation.LNavigation
import ca.gosyer.ui.reader.navigation.RightAndLeftNavigation
import ca.gosyer.ui.reader.navigation.navigationClickable
import ca.gosyer.ui.reader.viewer.ContinuousReader
import ca.gosyer.ui.reader.viewer.PagerReader
import ca.gosyer.uicore.components.ErrorScreen
import ca.gosyer.uicore.components.LoadingScreen
import ca.gosyer.uicore.components.mangaAspectRatio
import ca.gosyer.uicore.resources.stringResource
import ca.gosyer.uicore.vm.LocalViewModelFactory
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collectLatest

val supportedKeyList = listOf(
    Key.W,
    Key.DirectionUp,
    Key.S,
    Key.DirectionDown,
    Key.A,
    Key.DirectionLeft,
    Key.D,
    Key.DirectionRight
)

expect class ReaderLauncher {
    fun launch(
        chapterIndex: Int,
        mangaId: Long
    )

    @Composable
    fun Reader()
}

@Composable
expect fun rememberReaderLauncher(): ReaderLauncher

@Composable
fun ReaderMenu(
    chapterIndex: Int,
    mangaId: Long,
    hotkeyFlow: SharedFlow<KeyEvent>
) {
    val vmFactory = LocalViewModelFactory.current
    val vm = remember { vmFactory.instantiate<ReaderMenuViewModel>(ReaderMenuViewModel.Params(chapterIndex, mangaId)) }

    val state by vm.state.collectAsState()
    val previousChapter by vm.previousChapter.collectAsState()
    val chapter by vm.chapter.collectAsState()
    val nextChapter by vm.nextChapter.collectAsState()
    val pages by vm.pages.collectAsState()
    val readerModes by vm.readerModes.collectAsState()
    val readerMode by vm.readerMode.collectAsState()
    val continuous by vm.readerModeSettings.continuous.collectAsState()
    val direction by vm.readerModeSettings.direction.collectAsState()
    val padding by vm.readerModeSettings.padding.collectAsState()
    val imageScale by vm.readerModeSettings.imageScale.collectAsState()
    val fitSize by vm.readerModeSettings.fitSize.collectAsState()
    val maxSize by vm.readerModeSettings.maxSize.collectAsState()
    val navigationMode by vm.readerModeSettings.navigationMode.collectAsState()
    val currentPage by vm.currentPage.collectAsState()
    val currentPageOffset by vm.currentPageOffset.collectAsState()

    LaunchedEffect(hotkeyFlow) {
        hotkeyFlow.collectLatest {
            when (it.key) {
                Key.W, Key.DirectionUp -> vm.navigate(Navigation.PREV)
                Key.S, Key.DirectionDown -> vm.navigate(Navigation.NEXT)
                Key.A, Key.DirectionLeft -> vm.navigate(Navigation.LEFT)
                Key.D, Key.DirectionRight -> vm.navigate(Navigation.RIGHT)
            }
        }
    }
    DisposableEffect(Unit) {
        onDispose(vm::sendProgress)
    }

    Surface {
        Crossfade(state to chapter) { (state, chapter) ->
            if (state is ReaderChapter.State.Loaded && chapter != null) {
                if (pages.isNotEmpty()) {
                    var sideMenuOpen by remember { mutableStateOf(true) }
                    val sideMenuSize by animateDpAsState(
                        targetValue = if (sideMenuOpen) {
                            260.dp
                        } else {
                            0.dp
                        }
                    )

                    val loadingModifier = Modifier.fillMaxWidth().aspectRatio(mangaAspectRatio)
                    AnimatedVisibility(
                        sideMenuOpen,
                        enter = fadeIn() + slideInHorizontally(),
                        exit = fadeOut() + slideOutHorizontally()
                    ) {
                        ReaderSideMenu(
                            chapter = chapter,
                            currentPage = currentPage,
                            readerModes = readerModes,
                            selectedMode = readerMode,
                            onNewPageClicked = vm::navigate,
                            onCloseSideMenuClicked = {
                                sideMenuOpen = false
                            },
                            onSetReaderMode = vm::setMangaReaderMode,
                            onPrevChapterClicked = vm::prevChapter,
                            onNextChapterClicked = vm::nextChapter
                        )
                    }

                    Box(
                        Modifier.padding(start = sideMenuSize).fillMaxSize()
                    ) {
                        val readerModifier = Modifier
                            .navigationClickable(
                                navigation = navigationMode.toNavigation(),
                                onClick = vm::navigate
                            )

                        if (continuous) {
                            ContinuousReader(
                                readerModifier,
                                pages,
                                direction,
                                maxSize,
                                padding,
                                currentPage,
                                currentPageOffset,
                                previousChapter,
                                chapter,
                                nextChapter,
                                loadingModifier,
                                if (fitSize) {
                                    if (direction == Direction.Up || direction == Direction.Down) {
                                        ContentScale.FillWidth
                                    } else {
                                        ContentScale.FillHeight
                                    }
                                } else {
                                    ContentScale.Fit
                                },
                                vm.pageEmitter,
                                vm::retry,
                                vm::progress,
                                vm::updateLastPageReadOffset
                            )
                        } else {
                            PagerReader(
                                readerModifier,
                                direction,
                                currentPage,
                                pages,
                                previousChapter,
                                chapter,
                                nextChapter,
                                loadingModifier,
                                imageScale.toContentScale(),
                                vm.pageEmitter,
                                vm::retry,
                                vm::progress
                            )
                        }
                        SideMenuButton(sideMenuOpen, onOpenSideMenuClicked = { sideMenuOpen = true })
                    }
                } else {
                    ErrorScreen(stringResource(MR.strings.no_pages_found))
                }
            } else {
                LoadingScreen(
                    state is ReaderChapter.State.Wait || state is ReaderChapter.State.Loading,
                    errorMessage = (state as? ReaderChapter.State.Error)?.error?.message,
                    retry = vm::init
                )
            }
        }
    }
}

@Composable
fun SideMenuButton(sideMenuOpen: Boolean, onOpenSideMenuClicked: () -> Unit) {
    AnimatedVisibility(
        !sideMenuOpen,
        enter = fadeIn() + slideInHorizontally(),
        exit = fadeOut() + slideOutHorizontally()
    ) {
        IconButton(onOpenSideMenuClicked) {
            Icon(Icons.Rounded.ChevronRight, null)
        }
    }
}

@Composable
fun ReaderImage(
    imageIndex: Int,
    drawable: ImageBitmap?,
    progress: Float,
    status: ReaderPage.Status,
    error: String?,
    imageModifier: Modifier = Modifier.fillMaxSize(),
    loadingModifier: Modifier = imageModifier,
    contentScale: ContentScale = ContentScale.Fit,
    retry: (Int) -> Unit
) {
    Crossfade(drawable to status) { (drawable, status) ->
        if (drawable != null) {
            Image(
                drawable,
                modifier = imageModifier,
                contentDescription = null,
                contentScale = contentScale,
                filterQuality = FilterQuality.High
            )
        } else {
            LoadingScreen(status == ReaderPage.Status.QUEUE, loadingModifier, progress, error) { retry(imageIndex) }
        }
    }
}

@Composable
fun ChapterSeparator(
    previousChapter: ReaderChapter?,
    nextChapter: ReaderChapter?
) {
    Box(Modifier.fillMaxWidth().height(350.dp), contentAlignment = Alignment.Center) {
        Column {
            when {
                previousChapter == null && nextChapter != null -> {
                    Text(stringResource(MR.strings.no_previous_chapter))
                }
                previousChapter != null && nextChapter != null -> {
                    Text(stringResource(MR.strings.previous_chapter, previousChapter.chapter.name))
                    Spacer(Modifier.height(8.dp))
                    Text(stringResource(MR.strings.next_chapter, nextChapter.chapter.name))
                }
                previousChapter != null && nextChapter == null -> {
                    Text(stringResource(MR.strings.no_next_chapter))
                }
            }
        }
    }
}

fun NavigationMode.toNavigation() = when (this) {
    NavigationMode.RightAndLeftNavigation -> RightAndLeftNavigation()
    NavigationMode.KindlishNavigation -> KindlishNavigation()
    NavigationMode.LNavigation -> LNavigation()
    NavigationMode.EdgeNavigation -> EdgeNavigation()
}

fun ImageScale.toContentScale() = when (this) {
    ImageScale.FitScreen -> ContentScale.Inside
    ImageScale.FitHeight -> ContentScale.FillHeight
    ImageScale.FitWidth -> ContentScale.FillWidth
    ImageScale.OriginalSize -> ContentScale.None
    ImageScale.SmartFit -> ContentScale.Fit
    ImageScale.Stretch -> ContentScale.FillBounds
}
