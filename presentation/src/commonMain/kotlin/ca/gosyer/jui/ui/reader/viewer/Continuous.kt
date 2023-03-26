/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.reader.viewer

import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import ca.gosyer.jui.domain.reader.model.Direction
import ca.gosyer.jui.ui.base.model.StableHolder
import ca.gosyer.jui.ui.reader.ChapterSeparator
import ca.gosyer.jui.ui.reader.ReaderImage
import ca.gosyer.jui.ui.reader.model.MoveTo
import ca.gosyer.jui.ui.reader.model.PageMove
import ca.gosyer.jui.ui.reader.model.ReaderChapter
import ca.gosyer.jui.ui.reader.model.ReaderItem
import ca.gosyer.jui.ui.reader.model.ReaderPage
import ca.gosyer.jui.ui.reader.model.ReaderPageSeparator
import ca.gosyer.jui.uicore.components.HorizontalScrollbar
import ca.gosyer.jui.uicore.components.VerticalScrollbar
import ca.gosyer.jui.uicore.components.rememberScrollbarAdapter
import ca.gosyer.jui.uicore.components.scrollbarPadding
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest

@Composable
fun ContinuousReader(
    modifier: Modifier,
    pages: ImmutableList<ReaderItem>,
    direction: Direction,
    maxSize: Int,
    padding: Int,
    currentPage: ReaderItem?,
    currentPageOffset: Int,
    loadingModifier: Modifier,
    pageContentScale: ContentScale,
    pageEmitterHolder: StableHolder<SharedFlow<PageMove>>,
    retry: (ReaderPage) -> Unit,
    progress: (ReaderItem) -> Unit,
    updateLastPageReadOffset: (Int) -> Unit,
    requestPreloadChapter: (ReaderChapter) -> Unit,
) {
    BoxWithConstraints(modifier then Modifier.fillMaxSize()) {
        val state = rememberLazyListState(pages.indexOf(currentPage).coerceAtLeast(1), currentPageOffset)
        val density = LocalDensity.current
        LaunchedEffect(Unit) {
            pageEmitterHolder.item
                .mapLatest { pageMove ->
                    when (pageMove) {
                        is PageMove.Direction -> {
                            val (moveTo) = pageMove
                            val by = when (moveTo) {
                                MoveTo.Previous -> -maxHeight
                                MoveTo.Next -> maxHeight
                            } * 0.8F
                            state.animateScrollBy(
                                with(density) {
                                    by.toPx()
                                },
                            )
                            Unit
                        }
                        is PageMove.Page -> {
                            val pageNumber = pages.indexOf(pageMove.page)
                            if (pageNumber > -1) {
                                state.animateScrollToItem(pageNumber)
                            }
                        }
                    }
                }
                .launchIn(this)
        }
        DisposableEffect(Unit) {
            onDispose {
                updateLastPageReadOffset(state.firstVisibleItemScrollOffset)
            }
        }
        LaunchedEffect(state.layoutInfo.visibleItemsInfo.lastOrNull()?.index) {
            val index = state.layoutInfo.visibleItemsInfo.lastOrNull()?.index
            val page = index?.let { pages.getOrNull(it) }
            if (page != null) {
                progress(page)
            }
        }

        val imageModifier = if (maxSize != 0) {
            when (direction.isVertical) {
                true -> Modifier.width(maxSize.dp)
                false -> Modifier.height(maxSize.dp)
            }
        } else {
            Modifier
        }
        val contentPadding = when (direction) {
            Direction.Right -> PaddingValues(end = padding.dp)
            Direction.Left -> PaddingValues(start = padding.dp)
            Direction.Up -> PaddingValues(top = padding.dp)
            Direction.Down -> PaddingValues(bottom = padding.dp)
        }
        fun retry(index: Int) { pages.find { it is ReaderPage && it.index == index }?.let { retry(it as ReaderPage) } }

        when (direction.isVertical) {
            true -> {
                LazyColumn(
                    state = state,
                    reverseLayout = direction == Direction.Up,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    items(
                        modifier = Modifier.fillMaxWidth()
                            .padding(contentPadding),
                        pages = pages,
                        imageModifier = imageModifier,
                        loadingModifier = loadingModifier,
                        pageContentScale = pageContentScale,
                        retry = ::retry,
                        requestPreloadChapter = requestPreloadChapter,
                    )
                }
                VerticalScrollbar(
                    modifier = Modifier.align(Alignment.CenterEnd)
                        .fillMaxHeight()
                        .scrollbarPadding(),
                    adapter = rememberScrollbarAdapter(state),
                    reverseLayout = direction == Direction.Up,
                )
            }
            false -> {
                LazyRow(
                    state = state,
                    reverseLayout = direction == Direction.Left,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxHeight(),
                ) {
                    items(
                        modifier = Modifier.fillMaxHeight()
                            .padding(contentPadding),
                        pages = pages,
                        imageModifier = imageModifier,
                        loadingModifier = loadingModifier,
                        pageContentScale = pageContentScale,
                        retry = ::retry,
                        requestPreloadChapter = requestPreloadChapter,
                    )
                }
                HorizontalScrollbar(
                    modifier = Modifier.align(Alignment.BottomCenter)
                        .fillMaxWidth(),
                    adapter = rememberScrollbarAdapter(state),
                    reverseLayout = direction == Direction.Left,
                )
            }
        }
    }
}

private fun LazyListScope.items(
    modifier: Modifier,
    pages: ImmutableList<ReaderItem>,
    imageModifier: Modifier,
    loadingModifier: Modifier,
    pageContentScale: ContentScale,
    retry: (Int) -> Unit,
    requestPreloadChapter: (ReaderChapter) -> Unit,
) {
    items(
        pages,
        key = {
            when (it) {
                is ReaderPage -> it.chapter.chapter.index to it.index
                is ReaderPageSeparator -> it.previousChapter?.chapter?.index to it.nextChapter?.chapter?.index
            }
        },
    ) { image ->
        when (image) {
            is ReaderPage -> Box(modifier, contentAlignment = Alignment.Center) {
                ReaderImage(
                    imageIndex = image.index,
                    drawableHolder = image.bitmap.collectAsState().value,
                    bitmapInfo = image.bitmapInfo.collectAsState().value,
                    progress = image.progress.collectAsState().value,
                    status = image.status.collectAsState().value,
                    error = image.error.collectAsState().value,
                    imageModifier = imageModifier,
                    loadingModifier = loadingModifier,
                    contentScale = pageContentScale,
                    retry = retry,
                )
            }
            is ReaderPageSeparator -> ChapterSeparator(
                previousChapter = image.previousChapter,
                nextChapter = image.nextChapter,
                requestPreloadChapter = requestPreloadChapter,
            )
        }
    }
}
