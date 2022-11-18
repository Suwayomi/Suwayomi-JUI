/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.reader

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.key
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import ca.gosyer.jui.core.lang.withIOContext
import ca.gosyer.jui.domain.reader.model.Direction
import ca.gosyer.jui.domain.reader.model.ImageScale
import ca.gosyer.jui.domain.reader.model.NavigationMode
import ca.gosyer.jui.i18n.MR
import ca.gosyer.jui.ui.base.LocalViewModels
import ca.gosyer.jui.ui.base.model.StableHolder
import ca.gosyer.jui.ui.base.navigation.ActionItem
import ca.gosyer.jui.ui.base.navigation.BackHandler
import ca.gosyer.jui.ui.base.navigation.Toolbar
import ca.gosyer.jui.ui.reader.model.Navigation
import ca.gosyer.jui.ui.reader.model.PageMove
import ca.gosyer.jui.ui.reader.model.ReaderChapter
import ca.gosyer.jui.ui.reader.model.ReaderItem
import ca.gosyer.jui.ui.reader.model.ReaderPage
import ca.gosyer.jui.ui.reader.navigation.EdgeNavigation
import ca.gosyer.jui.ui.reader.navigation.KindlishNavigation
import ca.gosyer.jui.ui.reader.navigation.LNavigation
import ca.gosyer.jui.ui.reader.navigation.RightAndLeftNavigation
import ca.gosyer.jui.ui.reader.navigation.ViewerNavigation
import ca.gosyer.jui.ui.reader.navigation.navigationClickable
import ca.gosyer.jui.ui.reader.viewer.ContinuousReader
import ca.gosyer.jui.ui.reader.viewer.PagerReader
import ca.gosyer.jui.uicore.components.ErrorScreen
import ca.gosyer.jui.uicore.components.LoadingScreen
import ca.gosyer.jui.uicore.components.mangaAspectRatio
import ca.gosyer.jui.uicore.resources.stringResource
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

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
    hotkeyFlowHolder: StableHolder<SharedFlow<KeyEvent>>,
    onCloseRequest: () -> Unit
) {
    val viewModels = LocalViewModels.current
    val vm = remember { viewModels.readerViewModel(ReaderMenuViewModel.Params(chapterIndex, mangaId)) }
    DisposableEffect(vm) {
        onDispose(vm::onDispose)
    }
    DisposableEffect(Unit) {
        onDispose(vm::sendProgress)
    }

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
    val readerSettingsMenuOpen by vm.readerSettingsMenuOpen.collectAsState()

    LaunchedEffect(hotkeyFlowHolder) {
        hotkeyFlowHolder.item.collectLatest {
            when (it.key) {
                Key.W, Key.DirectionUp -> vm.navigate(Navigation.PREV)
                Key.S, Key.DirectionDown -> vm.navigate(Navigation.NEXT)
                Key.A, Key.DirectionLeft -> vm.navigate(Navigation.LEFT)
                Key.D, Key.DirectionRight -> vm.navigate(Navigation.RIGHT)
            }
        }
    }

    Surface {
        Crossfade(state to chapter) { (state, chapter) ->
            if (state is ReaderChapter.State.Loaded && chapter != null) {
                if (pages.isNotEmpty()) {
                    BoxWithConstraints {
                        if (maxWidth > 720.dp) {
                            WideReaderMenu(
                                chapter = chapter,
                                pages = pages,
                                readerModes = readerModes,
                                readerMode = readerMode,
                                continuous = continuous,
                                direction = direction,
                                padding = padding,
                                imageScale = imageScale,
                                fitSize = fitSize,
                                maxSize = maxSize,
                                navigationViewer = navigationMode.toNavigation(),
                                currentPage = currentPage,
                                currentPageOffset = currentPageOffset,
                                navigate = vm::navigate,
                                navigateTap = vm::navigate,
                                pageEmitterHolder = vm.pageEmitter,
                                retry = vm::retry,
                                progress = vm::progress,
                                updateLastPageReadOffset = vm::updateLastPageReadOffset,
                                sideMenuOpen = readerSettingsMenuOpen,
                                setSideMenuOpen = vm::setReaderSettingsMenuOpen,
                                setMangaReaderMode = vm::setMangaReaderMode,
                                movePrevChapter = vm::prevChapter,
                                moveNextChapter = vm::nextChapter
                            )
                        } else {
                            ThinReaderMenu(
                                previousChapter = previousChapter,
                                chapter = chapter,
                                nextChapter = nextChapter,
                                pages = pages,
                                readerModes = readerModes,
                                readerMode = readerMode,
                                continuous = continuous,
                                direction = direction,
                                padding = padding,
                                imageScale = imageScale,
                                fitSize = fitSize,
                                maxSize = maxSize,
                                navigationViewer = navigationMode.toNavigation(),
                                currentPage = currentPage,
                                currentPageOffset = currentPageOffset,
                                navigate = vm::navigate,
                                navigateTap = vm::navigate,
                                pageEmitterHolder = vm.pageEmitter,
                                retry = vm::retry,
                                progress = vm::progress,
                                updateLastPageReadOffset = vm::updateLastPageReadOffset,
                                readerMenuOpen = readerSettingsMenuOpen,
                                setMangaReaderMode = vm::setMangaReaderMode,
                                movePrevChapter = vm::prevChapter,
                                moveNextChapter = vm::nextChapter,
                                onCloseRequest = onCloseRequest
                            )
                        }
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
fun WideReaderMenu(
    chapter: ReaderChapter,
    pages: ImmutableList<ReaderItem>,
    readerModes: ImmutableList<String>,
    readerMode: String,
    continuous: Boolean,
    direction: Direction,
    padding: Int,
    imageScale: ImageScale,
    fitSize: Boolean,
    maxSize: Int,
    navigationViewer: ViewerNavigation,
    currentPage: Int,
    currentPageOffset: Int,
    navigate: (Int) -> Unit,
    navigateTap: (Navigation) -> Unit,
    pageEmitterHolder: StableHolder<SharedFlow<PageMove>>,
    retry: (ReaderPage) -> Unit,
    progress: (Int) -> Unit,
    updateLastPageReadOffset: (Int) -> Unit,
    sideMenuOpen: Boolean,
    setSideMenuOpen: (Boolean) -> Unit,
    setMangaReaderMode: (String) -> Unit,
    movePrevChapter: () -> Unit,
    moveNextChapter: () -> Unit
) {
    val sideMenuSize by animateDpAsState(
        targetValue = if (sideMenuOpen) {
            260.dp
        } else {
            0.dp
        }
    )

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
            onNewPageClicked = navigate,
            onCloseSideMenuClicked = {
                setSideMenuOpen(false)
            },
            onSetReaderMode = setMangaReaderMode,
            onPrevChapterClicked = movePrevChapter,
            onNextChapterClicked = moveNextChapter
        )
    }

    Box(
        Modifier.padding(start = sideMenuSize).fillMaxSize()
    ) {
        ReaderLayout(
            pages = pages,
            continuous = continuous,
            direction = direction,
            padding = padding,
            imageScale = imageScale,
            fitSize = fitSize,
            maxSize = maxSize,
            navigationViewer = navigationViewer,
            currentPage = currentPage,
            currentPageOffset = currentPageOffset,
            navigateTap = navigateTap,
            pageEmitterHolder = pageEmitterHolder,
            retry = retry,
            progress = progress,
            updateLastPageReadOffset = updateLastPageReadOffset
        )
        SideMenuButton(sideMenuOpen, onOpenSideMenuClicked = { setSideMenuOpen(true) })
    }
}

@Composable
fun ThinReaderMenu(
    previousChapter: ReaderChapter?,
    chapter: ReaderChapter,
    nextChapter: ReaderChapter?,
    pages: ImmutableList<ReaderItem>,
    readerModes: ImmutableList<String>,
    readerMode: String,
    continuous: Boolean,
    direction: Direction,
    padding: Int,
    imageScale: ImageScale,
    fitSize: Boolean,
    maxSize: Int,
    navigationViewer: ViewerNavigation,
    currentPage: Int,
    currentPageOffset: Int,
    navigate: (Int) -> Unit,
    navigateTap: (Navigation) -> Unit,
    pageEmitterHolder: StableHolder<SharedFlow<PageMove>>,
    retry: (ReaderPage) -> Unit,
    progress: (Int) -> Unit,
    updateLastPageReadOffset: (Int) -> Unit,
    readerMenuOpen: Boolean,
    setMangaReaderMode: (String) -> Unit,
    movePrevChapter: () -> Unit,
    moveNextChapter: () -> Unit,
    onCloseRequest: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(
        ModalBottomSheetValue.Hidden
    )
    val scope = rememberCoroutineScope()
    BackHandler(sheetState.isVisible) {
        scope.launch {
            sheetState.hide()
        }
    }
    ModalBottomSheetLayout(
        sheetState = sheetState,
        sheetContent = {
            ReaderSheet(
                readerModes = readerModes,
                selectedMode = readerMode,
                onSetReaderMode = setMangaReaderMode
            )
        }
    ) {
        Box {
            ReaderLayout(
                pages = pages,
                continuous = continuous,
                direction = direction,
                padding = padding,
                imageScale = imageScale,
                fitSize = fitSize,
                maxSize = maxSize,
                navigationViewer = navigationViewer,
                currentPage = currentPage,
                currentPageOffset = currentPageOffset,
                navigateTap = navigateTap,
                pageEmitterHolder = pageEmitterHolder,
                retry = retry,
                progress = progress,
                updateLastPageReadOffset = updateLastPageReadOffset
            )
            AnimatedVisibility(
                readerMenuOpen,
                enter = slideInVertically { -it },
                exit = slideOutVertically { -it },
                modifier = Modifier.align(Alignment.TopCenter)
            ) {
                Toolbar(
                    chapter.chapter.name,
                    closable = true,
                    onClose = onCloseRequest,
                    actions = {
                        listOf(
                            ActionItem(
                                stringResource(MR.strings.location_settings),
                                Icons.Rounded.Settings,
                                doAction = {
                                    scope.launch {
                                        sheetState.show()
                                    }
                                }
                            )
                        ).toImmutableList()
                    }
                )
            }
            ReaderExpandBottomMenu(
                modifier = Modifier.align(Alignment.BottomCenter),
                previousChapter = previousChapter,
                chapter = chapter,
                nextChapter = nextChapter,
                direction = direction,
                currentPage = currentPage,
                navigate = navigate,
                readerMenuOpen = readerMenuOpen,
                movePrevChapter = movePrevChapter,
                moveNextChapter = moveNextChapter
            )
        }
    }
}

@Composable
fun ReaderLayout(
    pages: ImmutableList<ReaderItem>,
    continuous: Boolean,
    direction: Direction,
    padding: Int,
    imageScale: ImageScale,
    fitSize: Boolean,
    maxSize: Int,
    navigationViewer: ViewerNavigation,
    currentPage: Int,
    currentPageOffset: Int,
    navigateTap: (Navigation) -> Unit,
    pageEmitterHolder: StableHolder<SharedFlow<PageMove>>,
    retry: (ReaderPage) -> Unit,
    progress: (Int) -> Unit,
    updateLastPageReadOffset: (Int) -> Unit
) {
    val loadingModifier = Modifier.widthIn(max = 700.dp)
        .fillMaxWidth()
        .aspectRatio(mangaAspectRatio)
    val readerModifier = Modifier
        .navigationClickable(
            navigation = navigationViewer,
            onClick = navigateTap
        )

    if (continuous) {
        ContinuousReader(
            modifier = readerModifier,
            pages = pages,
            direction = direction,
            maxSize = maxSize,
            padding = padding,
            currentPage = currentPage,
            currentPageOffset = currentPageOffset,
            loadingModifier = loadingModifier,
            pageContentScale = if (fitSize) {
                if (direction == Direction.Up || direction == Direction.Down) {
                    ContentScale.FillWidth
                } else {
                    ContentScale.FillHeight
                }
            } else {
                ContentScale.Fit
            },
            pageEmitterHolder = pageEmitterHolder,
            retry = retry,
            progress = progress,
            updateLastPageReadOffset = updateLastPageReadOffset
        )
    } else {
        PagerReader(
            parentModifier = readerModifier,
            direction = direction,
            currentPage = currentPage,
            pages = pages,
            loadingModifier = loadingModifier,
            pageContentScale = imageScale.toContentScale(),
            pageEmitterHolder = pageEmitterHolder,
            retry = retry,
            progress = progress
        )
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
    drawableHolder: StableHolder<(suspend () -> ReaderPage.ImageDecodeState)?>,
    progress: Float,
    status: ReaderPage.Status,
    error: String?,
    imageModifier: Modifier = Modifier.fillMaxSize(),
    loadingModifier: Modifier = imageModifier,
    contentScale: ContentScale = ContentScale.Fit,
    retry: (Int) -> Unit
) {
    Crossfade(drawableHolder to status) { (drawableHolder, status) ->
        val decodeState = produceState<ReaderPage.ImageDecodeState?>(null, drawableHolder) {
            val callback = drawableHolder.item
            if (callback != null) {
                withIOContext {
                    value = callback()
                }
            }
        }
        val decode = decodeState.value
        if (decode != null && decode is ReaderPage.ImageDecodeState.Success) {
            Image(
                bitmap = decode.bitmap,
                modifier = imageModifier,
                contentDescription = null,
                contentScale = contentScale,
                filterQuality = FilterQuality.High
            )
        } else {
            LoadingScreen(
                status == ReaderPage.Status.QUEUE || status == ReaderPage.Status.WORKING,
                loadingModifier,
                progress,
                error ?: when (decode) {
                    is ReaderPage.ImageDecodeState.FailedToDecode -> decode.exception.message
                    ReaderPage.ImageDecodeState.UnknownDecoder -> "Unknown decoder"
                    ReaderPage.ImageDecodeState.FailedToGetSnapShot -> "Failed to get snapshot"
                    else -> null
                }
            ) { retry(imageIndex) }
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
