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
import ca.gosyer.ui.reader.ChapterSeperator
import ca.gosyer.ui.reader.ReaderImage
import ca.gosyer.ui.reader.model.MoveTo
import ca.gosyer.ui.reader.model.ReaderChapter
import ca.gosyer.ui.reader.model.ReaderPage
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest

@Composable
fun ContinuousReader(
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
    pageEmitter: SharedFlow<Pair<MoveTo, Int>>,
    retry: (ReaderPage) -> Unit,
    progress: (Int) -> Unit,
    updateLastPageReadOffset: (Int) -> Unit
) {
    BoxWithConstraints {
        val state = rememberLazyListState(currentPage, currentPageOffset)
        LaunchedEffect(Unit) {
            pageEmitter
                .mapLatest { (moveTo) ->
                    val by = when (moveTo) {
                        MoveTo.Previous -> -maxHeight
                        MoveTo.Next -> maxHeight
                    }
                    state.animateScrollBy(by.value)
                }
                .launchIn(this)
        }
        DisposableEffect(Unit) {
            onDispose {
                updateLastPageReadOffset(state.firstVisibleItemScrollOffset)
            }
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
                        pages,
                        contentPadding,
                        previousChapter,
                        currentChapter,
                        nextChapter,
                        imageModifier,
                        loadingModifier,
                        pageContentScale,
                        retry,
                        progress
                    )
                }
            }
            Direction.Left, Direction.Right -> {
                LazyRow(
                    state = state,
                    reverseLayout = direction == Direction.Left,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxHeight()
                ) {
                    items(
                        pages,
                        contentPadding,
                        previousChapter,
                        currentChapter,
                        nextChapter,
                        imageModifier,
                        loadingModifier,
                        pageContentScale,
                        retry,
                        progress
                    )
                }
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
    retry: (ReaderPage) -> Unit,
    progress: (Int) -> Unit
) {
    item {
        LaunchedEffect(Unit) {
            progress(0)
        }
        ChapterSeperator(previousChapter, currentChapter)
    }
    items(pages) { image ->
        Box(Modifier.padding(paddingValues)) {
            LaunchedEffect(image.index) {
                progress(image.index)
            }
            ReaderImage(
                image.index,
                image.bitmap.collectAsState().value,
                image.progress.collectAsState().value,
                image.status.collectAsState().value,
                image.error.collectAsState().value,
                imageModifier,
                loadingModifier,
                pageContentScale
            ) { pageIndex ->
                pages.find { it.index == pageIndex }?.let { retry(it) }
            }
        }
    }
    item {
        LaunchedEffect(Unit) {
            progress(pages.size)
        }
        ChapterSeperator(currentChapter, nextChapter)
    }
}
