/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.reader

import androidx.compose.animation.Crossfade
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
import androidx.compose.runtime.CompositionLocalProvider
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
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.rememberWindowState
import ca.gosyer.build.BuildConfig
import ca.gosyer.common.di.AppScope
import ca.gosyer.data.reader.model.Direction
import ca.gosyer.data.reader.model.ImageScale
import ca.gosyer.data.reader.model.NavigationMode
import ca.gosyer.data.translation.XmlResourceBundle
import ca.gosyer.data.ui.UiPreferences
import ca.gosyer.data.ui.model.WindowSettings
import ca.gosyer.ui.base.components.ErrorScreen
import ca.gosyer.ui.base.components.LoadingScreen
import ca.gosyer.ui.base.components.LocalComposeWindow
import ca.gosyer.ui.base.components.mangaAspectRatio
import ca.gosyer.ui.base.components.setIcon
import ca.gosyer.ui.base.resources.LocalResources
import ca.gosyer.ui.base.resources.stringResource
import ca.gosyer.ui.base.theme.AppTheme
import ca.gosyer.ui.base.vm.viewModel
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
import ca.gosyer.util.lang.launchApplication
import io.kamel.core.config.KamelConfig
import io.kamel.image.config.LocalKamelConfig
import kotlinx.coroutines.DelicateCoroutinesApi

@OptIn(DelicateCoroutinesApi::class)
fun openReaderMenu(chapterIndex: Int, mangaId: Long) {
    val windowSettings = AppScope.getInstance<UiPreferences>()
        .readerWindow()
    val (
        position,
        size,
        placement
    ) = windowSettings.get().get()

    val resources = AppScope.getInstance<XmlResourceBundle>()
    val kamelConfig = AppScope.getInstance<KamelConfig>()

    launchApplication {
        var shortcuts by remember {
            mutableStateOf(emptyMap<Key, ((KeyEvent) -> Boolean)>())
        }
        val windowState = rememberWindowState(size = size, position = position, placement = placement)
        DisposableEffect(Unit) {
            onDispose {
                windowSettings.set(
                    WindowSettings(
                        windowState.position.x.value.toInt(),
                        windowState.position.y.value.toInt(),
                        windowState.size.width.value.toInt(),
                        windowState.size.height.value.toInt(),
                        windowState.placement == WindowPlacement.Maximized,
                        windowState.placement == WindowPlacement.Fullscreen
                    )
                )
            }
        }
        Window(
            onCloseRequest = ::exitApplication,
            title = "${BuildConfig.NAME} - Reader",
            state = windowState,
            onKeyEvent = {
                shortcuts[it.key]?.invoke(it) ?: false
            }
        ) {
            setIcon()
            CompositionLocalProvider(
                LocalComposeWindow provides window,
                LocalResources provides resources,
                LocalKamelConfig provides kamelConfig
            ) {
                AppTheme {
                    ReaderMenu(chapterIndex, mangaId) { shortcuts = it }
                }
            }
        }
    }
}

@Composable
fun ReaderMenu(
    chapterIndex: Int,
    mangaId: Long,
    setHotkeys: (Map<Key, ((KeyEvent) -> Boolean)>) -> Unit
) {
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
    val imageScale by vm.readerModeSettings.imageScale.collectAsState()
    val fitSize by vm.readerModeSettings.fitSize.collectAsState()
    val maxSize by vm.readerModeSettings.maxSize.collectAsState()
    val navigationMode by vm.readerModeSettings.navigationMode.collectAsState()
    val currentPage by vm.currentPage.collectAsState()
    val currentPageOffset by vm.currentPageOffset.collectAsState()

    fun hotkey(block: () -> Unit): (KeyEvent) -> Boolean {
        return {
            block()
            true
        }
    }

    LaunchedEffect(Unit) {
        setHotkeys(
            mapOf(
                Key.W to hotkey { vm.navigate(Navigation.PREV) },
                Key.DirectionUp to hotkey { vm.navigate(Navigation.PREV) },
                Key.S to hotkey { vm.navigate(Navigation.NEXT) },
                Key.DirectionDown to hotkey { vm.navigate(Navigation.NEXT) },
                Key.A to hotkey { vm.navigate(Navigation.LEFT) },
                Key.DirectionLeft to hotkey { vm.navigate(Navigation.LEFT) },
                Key.D to hotkey { vm.navigate(Navigation.RIGHT) },
                Key.DirectionRight to hotkey { vm.navigate(Navigation.RIGHT) }
            )
        )
    }
    DisposableEffect(Unit) {
        onDispose(vm::sendProgress)
    }

    Surface {
        Crossfade(state to chapter) { (state, chapter) ->
            if (state is ReaderChapter.State.Loaded && chapter != null) {
                Box(
                    Modifier.fillMaxSize()
                        .navigationClickable(navigationMode.toNavigation()) {
                            vm.navigate(it)
                        }
                ) {
                    val loadingModifier = Modifier.fillMaxWidth().aspectRatio(mangaAspectRatio)
                    if (pages.isNotEmpty()) {
                        if (continuous) {
                            ContinuousReader(
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
                    } else {
                        ErrorScreen(stringResource("no_pages_found"))
                    }
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
                    Text(stringResource("no_previous_chapter"))
                }
                previousChapter != null && nextChapter != null -> {
                    Text(stringResource("previous_chapter", previousChapter.chapter.name))
                    Spacer(Modifier.height(8.dp))
                    Text(stringResource("next_chapter", nextChapter.chapter.name))
                }
                previousChapter != null && nextChapter == null -> {
                    Text(stringResource("no_next_chapter"))
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
