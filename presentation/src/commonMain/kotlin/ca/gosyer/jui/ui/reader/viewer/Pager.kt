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
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import ca.gosyer.jui.domain.reader.model.Direction
import ca.gosyer.jui.ui.base.model.StableHolder
import ca.gosyer.jui.ui.reader.ChapterSeparator
import ca.gosyer.jui.ui.reader.ReaderImage
import ca.gosyer.jui.ui.reader.model.MoveTo
import ca.gosyer.jui.ui.reader.model.PageMove
import ca.gosyer.jui.ui.reader.model.ReaderItem
import ca.gosyer.jui.ui.reader.model.ReaderPage
import ca.gosyer.jui.ui.reader.model.ReaderPageSeparator
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.VerticalPager
import com.google.accompanist.pager.rememberPagerState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest

@Composable
fun PagerReader(
    parentModifier: Modifier,
    direction: Direction,
    currentPage: ReaderItem?,
    pages: ImmutableList<ReaderItem>,
    loadingModifier: Modifier,
    pageContentScale: ContentScale,
    pageEmitterHolder: StableHolder<SharedFlow<PageMove>>,
    retry: (ReaderPage) -> Unit,
    progress: (ReaderItem) -> Unit
) {
    val state = rememberPagerState(initialPage = pages.indexOf(currentPage).coerceAtLeast(1))
    val currentPageState = rememberUpdatedState(currentPage)

    LaunchedEffect(pages.size, state, currentPageState) {
        val pageRange = 0..(pages.size + 1)
        pageEmitterHolder.item
            .mapLatest { pageMove ->
                when (pageMove) {
                    is PageMove.Direction -> {
                        val currentPage = currentPageState.value ?: return@mapLatest
                        val (moveTo) = pageMove
                        val page = when (moveTo) {
                            MoveTo.Previous -> pages.indexOf(currentPage) - 1
                            MoveTo.Next -> pages.indexOf(currentPage) + 1
                        }
                        if (page in pageRange) {
                            state.animateScrollToPage(page)
                        }
                    }
                    is PageMove.Page -> {
                        val pageNumber = pages.indexOf(pageMove.page)
                        if (pageNumber > -1) {
                            state.animateScrollToPage(pageNumber)
                        }
                    }
                }
            }
            .launchIn(this)
    }

    LaunchedEffect(state.currentPage) {
        val page = pages.getOrNull(state.currentPage)
        if (page != null) {
            progress(page)
        }
    }
    val modifier = parentModifier then Modifier.fillMaxSize()
    fun retry(index: Int) { pages.find { it is ReaderPage && it.index == index }?.let { retry(it as ReaderPage) } }

    if (direction == Direction.Down || direction == Direction.Up) {
        VerticalPager(
            count = pages.size,
            state = state,
            reverseLayout = direction == Direction.Up,
            modifier = modifier
        ) {
            HandlePager(
                pages = pages,
                page = it,
                loadingModifier = loadingModifier,
                pageContentScale = pageContentScale,
                retry = ::retry
            )
        }
    } else {
        HorizontalPager(
            count = pages.size,
            state = state,
            reverseLayout = direction == Direction.Left,
            modifier = modifier
        ) {
            HandlePager(
                pages = pages,
                page = it,
                loadingModifier = loadingModifier,
                pageContentScale = pageContentScale,
                retry = ::retry
            )
        }
    }
}

@Composable
fun HandlePager(
    pages: ImmutableList<ReaderItem>,
    page: Int,
    loadingModifier: Modifier,
    pageContentScale: ContentScale,
    retry: (Int) -> Unit
) {
    when (val image = pages[page]) {
        is ReaderPage -> {
            ReaderImage(
                imageIndex = image.index,
                drawableHolder = image.bitmap.collectAsState().value,
                progress = image.progress.collectAsState().value,
                status = image.status.collectAsState().value,
                error = image.error.collectAsState().value,
                loadingModifier = loadingModifier,
                retry = retry,
                contentScale = pageContentScale
            )
        }
        is ReaderPageSeparator -> ChapterSeparator(image.previousChapter, image.nextChapter)
    }
}
