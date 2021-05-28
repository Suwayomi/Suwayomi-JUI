/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.reader

import ca.gosyer.data.reader.ReaderModeWatch
import ca.gosyer.data.reader.ReaderPreferences
import ca.gosyer.data.reader.model.Direction
import ca.gosyer.data.server.interactions.ChapterInteractionHandler
import ca.gosyer.ui.base.vm.ViewModel
import ca.gosyer.ui.reader.model.MoveTo
import ca.gosyer.ui.reader.model.Navigation
import ca.gosyer.ui.reader.model.ReaderChapter
import ca.gosyer.ui.reader.model.ReaderPage
import ca.gosyer.ui.reader.model.ViewerChapters
import ca.gosyer.util.lang.throwIfCancellation
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

class ReaderMenuViewModel @Inject constructor(
    params: Params,
    private val readerPreferences: ReaderPreferences,
    private val chapterHandler: ChapterInteractionHandler
) : ViewModel() {
    private val viewerChapters = ViewerChapters(
        MutableStateFlow(null),
        MutableStateFlow(null),
        MutableStateFlow(null)
    )
    val previousChapter = viewerChapters.prevChapter.asStateFlow()
    val chapter = viewerChapters.currChapter.asStateFlow()
    val nextChapter = viewerChapters.nextChapter.asStateFlow()

    private val _state = MutableStateFlow<ReaderChapter.State>(ReaderChapter.State.Wait)
    val state = _state.asStateFlow()

    private val _pages = MutableStateFlow(emptyList<ReaderPage>())
    val pages = _pages.asStateFlow()

    private val _currentPage = MutableStateFlow(1)
    val currentPage = _currentPage.asStateFlow()

    private val _pageEmitter = MutableSharedFlow<Pair<MoveTo, Int>>()
    val pageEmitter = _pageEmitter.asSharedFlow()

    val readerModeSettings = ReaderModeWatch(readerPreferences, scope)

    private val loader = ChapterLoader(scope.coroutineContext, readerPreferences, chapterHandler)

    init {
        scope.launch(Dispatchers.Default) {
            init(params.mangaId, params.chapterIndex)
        }
    }

    fun navigate(navigationRegion: Navigation) {
        scope.launch {
            val moveTo = when (navigationRegion) {
                Navigation.NONE -> null
                Navigation.NEXT -> MoveTo.Next
                Navigation.PREV -> MoveTo.Previous
                Navigation.RIGHT -> when (readerModeSettings.direction.value) {
                    Direction.Left -> MoveTo.Previous
                    else -> MoveTo.Next
                }
                Navigation.LEFT -> when (readerModeSettings.direction.value) {
                    Direction.Left -> MoveTo.Next
                    else -> MoveTo.Previous
                }
            }
            if (moveTo != null) {
                moveDirection(moveTo)
            }
        }
    }

    private suspend fun moveDirection(direction: MoveTo) {
        _pageEmitter.emit(direction to currentPage.value)
    }

    fun progress(index: Int) {
        _currentPage.value = index
    }

    fun retry(page: ReaderPage) {
        chapter.value?.pageLoader?.retryPage(page)
    }

    private fun resetValues() {
        _pages.value = emptyList()
        _currentPage.value = 1
        _state.value = ReaderChapter.State.Wait
        viewerChapters.recycle()
    }

    suspend fun init(mangaId: Long, chapterIndex: Int) {
        resetValues()
        val chapter = ReaderChapter(
            scope.coroutineContext + Dispatchers.Default,
            try {
                chapterHandler.getChapter(mangaId, chapterIndex)
            } catch (e: Exception) {
                e.throwIfCancellation()
                _state.value = ReaderChapter.State.Error(e)
                throw e
            }
        )
        val pages = loader.loadChapter(chapter)
        viewerChapters.currChapter.value = chapter
        scope.launch(Dispatchers.Default) {
            val chapters = try {
                chapterHandler.getChapters(mangaId)
            } catch (e: Exception) {
                e.throwIfCancellation()
                emptyList()
            }
            val nextChapter = chapters.find { it.index == chapterIndex + 1 }
            if (nextChapter != null) {
                viewerChapters.nextChapter.value = ReaderChapter(
                    scope.coroutineContext + Dispatchers.Default,
                    nextChapter
                )
            }
            val prevChapter = chapters.find { it.index == chapterIndex - 1 }
            if (prevChapter != null) {
                viewerChapters.prevChapter.value = ReaderChapter(
                    scope.coroutineContext + Dispatchers.Default,
                    prevChapter
                )
            }
        }
        val lastPageRead = chapter.chapter.lastPageRead
        if (lastPageRead != 0) {
            _currentPage.value = chapter.chapter.lastPageRead
        }

        chapter.stateObserver
            .onEach {
                _state.value = it
            }
            .launchIn(chapter.scope)
        pages
            .onEach { pageList ->
                pageList.forEach { it.chapter = chapter }
                _pages.value = pageList
            }
            .launchIn(chapter.scope)

        _currentPage
            .onEach { index ->
                if (index == pages.value.size) {
                    markChapterRead(mangaId, chapter)
                } else {
                    pages.value.getOrNull(index - 1)?.let { chapter.pageLoader?.loadPage(it) }
                }
            }
            .launchIn(chapter.scope)
    }

    private suspend fun markChapterRead(mangaId: Long, chapter: ReaderChapter) {
        chapterHandler.updateChapter(mangaId, chapter.chapter.index, true)
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun sendProgress() {
        val chapter = chapter.value?.chapter ?: return
        if (chapter.read) return
        GlobalScope.launch {
            chapterHandler.updateChapter(chapter.mangaId, chapter.index, lastPageRead = currentPage.value)
        }
    }

    data class Params(val chapterIndex: Int, val mangaId: Long)
}
