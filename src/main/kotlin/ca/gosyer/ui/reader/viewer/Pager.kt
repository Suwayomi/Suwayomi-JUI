/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.reader.viewer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import ca.gosyer.data.reader.model.Direction
import ca.gosyer.ui.reader.ChapterSeperator
import ca.gosyer.ui.reader.ReaderImage
import ca.gosyer.ui.reader.model.MoveTo
import ca.gosyer.ui.reader.model.ReaderChapter
import ca.gosyer.ui.reader.model.ReaderPage
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.VerticalPager
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest

@Composable
fun PagerReader(
    direction: Direction,
    currentPage: Int,
    pages: List<ReaderPage>,
    previousChapter: ReaderChapter?,
    currentChapter: ReaderChapter,
    nextChapter: ReaderChapter?,
    loadingModifier: Modifier,
    pageContentScale: ContentScale,
    pageEmitter: SharedFlow<Pair<MoveTo, Int>>,
    retry: (ReaderPage) -> Unit,
    progress: (Int) -> Unit
) {
    val state = rememberPagerState(pages.size + 1, initialPage = currentPage)

    LaunchedEffect(Unit) {
        pageEmitter
            .mapLatest { (moveTo, currentPage) ->
                val page = when (moveTo) {
                    MoveTo.Previous -> currentPage - 1
                    MoveTo.Next -> currentPage + 1
                }
                if (page <= pages.size) {
                    state.animateScrollToPage(page)
                }
            }
            .launchIn(this)
    }

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
                loadingModifier,
                pageContentScale,
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
                loadingModifier,
                pageContentScale,
                retry
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
                image.progress.collectAsState().value,
                image.status.collectAsState().value,
                image.error.collectAsState().value,
                loadingModifier = loadingModifier,
                retry = { pageIndex ->
                    pages.find { it.index == pageIndex }?.let { retry(it) }
                },
                contentScale = pageContentScale
            )
        }
    }
}
