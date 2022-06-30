/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.reader.viewer

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import ca.gosyer.jui.domain.reader.model.Direction
import ca.gosyer.jui.ui.reader.ChapterSeparator
import ca.gosyer.jui.ui.reader.ReaderImage
import ca.gosyer.jui.ui.reader.model.MoveTo
import ca.gosyer.jui.ui.reader.model.PageMove
import ca.gosyer.jui.ui.reader.model.ReaderChapter
import ca.gosyer.jui.ui.reader.model.ReaderPage
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.VerticalPager
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest

@Composable
fun PagerReader(
    parentModifier: Modifier,
    direction: Direction,
    currentPage: Int,
    pages: List<ReaderPage>,
    previousChapter: ReaderChapter?,
    currentChapter: ReaderChapter,
    nextChapter: ReaderChapter?,
    loadingModifier: Modifier,
    pageContentScale: ContentScale,
    pageEmitter: SharedFlow<PageMove>,
    retry: (ReaderPage) -> Unit,
    progress: (Int) -> Unit
) {
    val state = rememberPagerState(initialPage = currentPage)

    LaunchedEffect(pages.size) {
        val pageRange = 0..(pages.size + 1)
        pageEmitter
            .mapLatest { pageMove ->
                when (pageMove) {
                    is PageMove.Direction -> {
                        val (moveTo, currentPage) = pageMove
                        val page = when (moveTo) {
                            MoveTo.Previous -> currentPage - 1
                            MoveTo.Next -> currentPage + 1
                        }
                        if (page in pageRange) {
                            state.animateScrollToPage(page)
                        }
                    }
                    is PageMove.Page -> {
                        val (pageNumber) = pageMove
                        if (pageNumber in pageRange) {
                            state.animateScrollToPage(pageNumber)
                        }
                    }
                }
            }
            .launchIn(this)
    }

    LaunchedEffect(state.currentPage) {
        if (state.currentPage != currentPage) {
            progress(state.currentPage)
        }
    }
    val modifier = parentModifier then Modifier.fillMaxSize()
    fun retry(index: Int) { pages.find { it.index == index }?.let { retry(it) } }

    if (direction == Direction.Down || direction == Direction.Up) {
        VerticalPager(
            count = pages.size + 2,
            state = state,
            reverseLayout = direction == Direction.Up,
            modifier = modifier
        ) {
            HandlePager(
                pages = pages,
                page = it,
                previousChapter = previousChapter,
                currentChapter = currentChapter,
                nextChapter = nextChapter,
                loadingModifier = loadingModifier,
                pageContentScale = pageContentScale,
                retry = ::retry
            )
        }
    } else {
        HorizontalPager(
            count = pages.size + 2,
            state = state,
            reverseLayout = direction == Direction.Left,
            modifier = modifier
        ) {
            HandlePager(
                pages = pages,
                page = it,
                previousChapter = previousChapter,
                currentChapter = currentChapter,
                nextChapter = nextChapter,
                loadingModifier = loadingModifier,
                pageContentScale = pageContentScale,
                retry = ::retry
            )
        }
    }
}

@Composable
fun HandlePager(
    pages: List<ReaderPage>,
    page: Int,
    previousChapter: ReaderChapter?,
    currentChapter: ReaderChapter,
    nextChapter: ReaderChapter?,
    loadingModifier: Modifier,
    pageContentScale: ContentScale,
    retry: (Int) -> Unit,
) {
    when (page) {
        0 -> ChapterSeparator(previousChapter, currentChapter)
        pages.size + 1 -> ChapterSeparator(currentChapter, nextChapter)
        else -> {
            val image = pages[page - 1]
            ReaderImage(
                imageIndex = image.index,
                drawable = image.bitmap.collectAsState().value,
                progress = image.progress.collectAsState().value,
                status = image.status.collectAsState().value,
                error = image.error.collectAsState().value,
                loadingModifier = loadingModifier,
                retry = retry,
                contentScale = pageContentScale
            )
        }
    }
}
