/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.reader.viewer

import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
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
    previousChapter: ReaderChapter?,
    currentChapter: ReaderChapter,
    nextChapter: ReaderChapter?,
    pageModifier: Modifier,
    pageEmitter: SharedFlow<Pair<MoveTo, Int>>,
    retry: (ReaderPage) -> Unit,
    progress: (Int) -> Unit
) {
    BoxWithConstraints {
        val state = rememberLazyListState(1)
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

        LazyColumn(state = state) {
            item {
                LaunchedEffect(Unit) {
                    progress(0)
                }
                ChapterSeperator(previousChapter, currentChapter)
            }
            items(pages) { image ->
                LaunchedEffect(image.index) {
                    progress(image.index)
                }
                ReaderImage(
                    image.index,
                    image.bitmap.collectAsState().value,
                    image.status.collectAsState().value,
                    image.error.collectAsState().value,
                    loadingModifier = pageModifier,
                    retry = { pageIndex ->
                        pages.find { it.index == pageIndex }?.let { retry(it) }
                    }
                )
            }
            item {
                LaunchedEffect(Unit) {
                    progress(pages.size)
                }
                ChapterSeperator(currentChapter, nextChapter)
            }
        }
    }
}
