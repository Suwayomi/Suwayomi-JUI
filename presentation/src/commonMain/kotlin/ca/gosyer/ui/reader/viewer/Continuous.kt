/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.reader.viewer

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
import androidx.compose.ui.unit.dp
import ca.gosyer.data.reader.model.Direction
import ca.gosyer.ui.reader.ChapterSeparator
import ca.gosyer.ui.reader.ReaderImage
import ca.gosyer.ui.reader.model.MoveTo
import ca.gosyer.ui.reader.model.PageMove
import ca.gosyer.ui.reader.model.ReaderChapter
import ca.gosyer.ui.reader.model.ReaderPage
import ca.gosyer.uicore.components.HorizontalScrollbar
import ca.gosyer.uicore.components.VerticalScrollbar
import ca.gosyer.uicore.components.rememberScrollbarAdapter
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest

@Composable
fun ContinuousReader(
    modifier: Modifier,
    pages: List<ReaderPage>,
    direction: Direction,
    maxSize: Int,
    padding: Int,
    currentPage: Int,
    currentPageOffset: Int,
    previousChapter: ReaderChapter?,
    currentChapter: ReaderChapter,
    nextChapter: ReaderChapter?,
    loadingModifier: Modifier,
    pageContentScale: ContentScale,
    pageEmitter: SharedFlow<PageMove>,
    retry: (ReaderPage) -> Unit,
    progress: (Int) -> Unit,
    updateLastPageReadOffset: (Int) -> Unit
) {
    BoxWithConstraints(modifier then Modifier.fillMaxSize()) {
        val state = rememberLazyListState(currentPage, currentPageOffset)
        LaunchedEffect(Unit) {
            pageEmitter
                .mapLatest { pageMove ->
                    when (pageMove) {
                        is PageMove.Direction -> {
                            val (moveTo) = pageMove
                            val by = when (moveTo) {
                                MoveTo.Previous -> -maxHeight
                                MoveTo.Next -> maxHeight
                            } * 0.8F
                            state.animateScrollBy(by.value)
                            Unit
                        }
                        is PageMove.Page -> {
                            val (pageNumber) = pageMove
                            if (pageNumber in 0..pages.size) {
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
        LaunchedEffect(state.firstVisibleItemIndex) {
            progress(state.firstVisibleItemIndex)
        }

        val imageModifier = if (maxSize != 0) {
            when (direction) {
                Direction.Up, Direction.Down -> Modifier.width(maxSize.dp)
                Direction.Left, Direction.Right -> Modifier.height(maxSize.dp)
            }
        } else Modifier
        val contentPadding = when (direction) {
            Direction.Right -> PaddingValues(end = padding.dp)
            Direction.Left -> PaddingValues(start = padding.dp)
            Direction.Up -> PaddingValues(top = padding.dp)
            Direction.Down -> PaddingValues(bottom = padding.dp)
        }

        when (direction) {
            Direction.Down, Direction.Up -> {
                LazyColumn(
                    state = state,
                    reverseLayout = direction == Direction.Up,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(
                        pages = pages,
                        paddingValues = contentPadding,
                        previousChapter = previousChapter,
                        currentChapter = currentChapter,
                        nextChapter = nextChapter,
                        imageModifier = imageModifier,
                        loadingModifier = loadingModifier,
                        pageContentScale = pageContentScale,
                        retry = retry
                    )
                }
                VerticalScrollbar(
                    modifier = Modifier.align(Alignment.CenterEnd)
                        .fillMaxHeight()
                        .padding(horizontal = 4.dp, vertical = 8.dp),
                    adapter = rememberScrollbarAdapter(state),
                    reverseLayout = direction == Direction.Up
                )
            }
            Direction.Left, Direction.Right -> {
                LazyRow(
                    state = state,
                    reverseLayout = direction == Direction.Left,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxHeight()
                ) {
                    items(
                        pages = pages,
                        paddingValues = contentPadding,
                        previousChapter = previousChapter,
                        currentChapter = currentChapter,
                        nextChapter = nextChapter,
                        imageModifier = imageModifier,
                        loadingModifier = loadingModifier,
                        pageContentScale = pageContentScale,
                        retry = retry
                    )
                }
                HorizontalScrollbar(
                    modifier = Modifier.align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    adapter = rememberScrollbarAdapter(state),
                    reverseLayout = direction == Direction.Left
                )
            }
        }
    }
}

private fun LazyListScope.items(
    pages: List<ReaderPage>,
    paddingValues: PaddingValues,
    previousChapter: ReaderChapter?,
    currentChapter: ReaderChapter,
    nextChapter: ReaderChapter?,
    imageModifier: Modifier,
    loadingModifier: Modifier,
    pageContentScale: ContentScale,
    retry: (ReaderPage) -> Unit
) {
    item {
        ChapterSeparator(previousChapter, currentChapter)
    }
    items(pages) { image ->
        Box(Modifier.padding(paddingValues)) {
            ReaderImage(
                imageIndex = image.index,
                drawable = image.bitmap.collectAsState().value,
                progress = image.progress.collectAsState().value,
                status = image.status.collectAsState().value,
                error = image.error.collectAsState().value,
                imageModifier = imageModifier,
                loadingModifier = loadingModifier,
                contentScale = pageContentScale
            ) { pageIndex ->
                pages.find { it.index == pageIndex }?.let { retry(it) }
            }
        }
    }
    item {
        ChapterSeparator(previousChapter = currentChapter, nextChapter = nextChapter)
    }
}
