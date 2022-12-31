/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.reader

import ca.gosyer.jui.core.lang.launchDefault
import ca.gosyer.jui.core.prefs.getAsFlow
import ca.gosyer.jui.domain.chapter.interactor.GetChapter
import ca.gosyer.jui.domain.chapter.interactor.GetChapterPage
import ca.gosyer.jui.domain.chapter.interactor.GetChapters
import ca.gosyer.jui.domain.chapter.interactor.UpdateChapterLastPageRead
import ca.gosyer.jui.domain.chapter.interactor.UpdateChapterMeta
import ca.gosyer.jui.domain.chapter.interactor.UpdateChapterRead
import ca.gosyer.jui.domain.chapter.model.Chapter
import ca.gosyer.jui.domain.manga.interactor.GetManga
import ca.gosyer.jui.domain.manga.interactor.UpdateMangaMeta
import ca.gosyer.jui.domain.manga.model.Manga
import ca.gosyer.jui.domain.manga.model.MangaMeta
import ca.gosyer.jui.domain.reader.ReaderModeWatch
import ca.gosyer.jui.domain.reader.model.Direction
import ca.gosyer.jui.domain.reader.service.ReaderPreferences
import ca.gosyer.jui.ui.base.ChapterCache
import ca.gosyer.jui.ui.base.image.BitmapDecoderFactory
import ca.gosyer.jui.ui.base.model.StableHolder
import ca.gosyer.jui.ui.reader.loader.PagesState
import ca.gosyer.jui.ui.reader.model.MoveTo
import ca.gosyer.jui.ui.reader.model.Navigation
import ca.gosyer.jui.ui.reader.model.PageMove
import ca.gosyer.jui.ui.reader.model.ReaderChapter
import ca.gosyer.jui.ui.reader.model.ReaderItem
import ca.gosyer.jui.ui.reader.model.ReaderPage
import ca.gosyer.jui.ui.reader.model.ReaderPageSeparator
import ca.gosyer.jui.ui.reader.model.ViewerChapters
import ca.gosyer.jui.uicore.vm.ContextWrapper
import ca.gosyer.jui.uicore.vm.ViewModel
import io.ktor.http.decodeURLQueryComponent
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.singleOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Inject
import org.lighthousegames.logging.logging

class ReaderMenuViewModel @Inject constructor(
    private val readerPreferences: ReaderPreferences,
    private val getManga: GetManga,
    private val getChapters: GetChapters,
    private val getChapter: GetChapter,
    private val getChapterPage: GetChapterPage,
    private val updateChapterRead: UpdateChapterRead,
    private val updateChapterLastPageRead: UpdateChapterLastPageRead,
    private val updateMangaMeta: UpdateMangaMeta,
    private val updateChapterMeta: UpdateChapterMeta,
    private val chapterCache: ChapterCache,
    contextWrapper: ContextWrapper,
    private val params: Params
) : ViewModel(contextWrapper) {
    override val scope = MainScope()
    private val _manga = MutableStateFlow<Manga?>(null)
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

    private val _pages = MutableStateFlow<ImmutableList<ReaderItem>>(persistentListOf())
    val pages = _pages.asStateFlow()

    private val _currentPage = MutableStateFlow<ReaderItem?>(null)
    val currentPage = _currentPage.asStateFlow()

    private val _currentPageOffset = MutableStateFlow(1)
    val currentPageOffset = _currentPageOffset.asStateFlow()

    private val _readerSettingsMenuOpen = MutableStateFlow(false)
    val readerSettingsMenuOpen = _readerSettingsMenuOpen.asStateFlow()

    private val _pageEmitter = MutableSharedFlow<PageMove>()
    val pageEmitter = StableHolder(_pageEmitter.asSharedFlow())

    val readerModes = readerPreferences.modes()
        .getAsFlow()
        .map { it.toImmutableList() }
        .stateIn(scope, SharingStarted.Eagerly, persistentListOf())
    val readerMode = combine(readerPreferences.mode().getAsFlow(), _manga) { mode, manga ->
        val mangaMode = manga?.meta?.juiReaderMode?.decodeURLQueryComponent()
        if (
            mangaMode != null &&
            mangaMode != MangaMeta.DEFAULT_READER_MODE &&
            mangaMode in readerModes.value
        ) {
            mangaMode
        } else {
            mode
        }
    }.stateIn(scope, SharingStarted.Eagerly, readerPreferences.mode().get())

    val readerModeSettings = ReaderModeWatch(readerPreferences, scope, readerMode)

    private val loader = ChapterLoader(
        readerPreferences = readerPreferences,
        getChapterPage = getChapterPage,
        chapterCache = chapterCache,
        bitmapDecoderFactory = BitmapDecoderFactory(contextWrapper)
    )

    init {
        init()
    }

    fun init() {
        scope.launchDefault {
            runCatching {
                initManga(params.mangaId)
                initChapters(params.mangaId, params.chapterIndex)
            }
        }
    }

    fun navigate(navigationRegion: Navigation): Boolean {
        scope.launch {
            val moveTo = when (navigationRegion) {
                Navigation.MENU -> {
                    setReaderSettingsMenuOpen(!readerSettingsMenuOpen.value)
                    null
                }
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
                Navigation.DOWN -> when (readerModeSettings.direction.value) {
                    Direction.Up -> MoveTo.Previous
                    else -> MoveTo.Next
                }
                Navigation.UP -> when (readerModeSettings.direction.value) {
                    Direction.Up -> MoveTo.Next
                    else -> MoveTo.Previous
                }
            }
            if (moveTo != null) {
                _pageEmitter.emit(PageMove.Direction(moveTo))
            }
        }
        return true
    }

    fun navigate(page: Int) {
        log.info { "Navigate to $page" }
        scope.launch {
            _pageEmitter.emit(PageMove.Page(pages.value.getOrNull(page) ?: return@launch))
        }
    }

    fun progress(page: ReaderItem) {
        log.info { "Progressed to $page" }
        _currentPage.value = page
    }

    fun retry(page: ReaderPage) {
        log.info { "Retrying ${page.index}" }
        chapter.value?.pageLoader?.retryPage(page)
    }

    private fun resetValues() {
        viewerChapters.recycle()
        _pages.value = persistentListOf()
        _currentPage.value = null
    }

    fun setMangaReaderMode(mode: String) {
        scope.launchDefault {
            _manga.value?.let {
                updateMangaMeta.await(it, mode, onError = { toast(it.message.orEmpty()) })
            }
            initManga(params.mangaId)
        }
    }

    fun setReaderSettingsMenuOpen(open: Boolean) {
        _readerSettingsMenuOpen.value = open
    }

    fun prevChapter() {
        scope.launchDefault {
            val prevChapter = previousChapter.value ?: return@launchDefault
            try {
                _state.value = ReaderChapter.State.Wait
                sendProgress()
                viewerChapters.movePrev()
                initChapters(params.mangaId, prevChapter.chapter.index, fromMenuButton = true)
            } catch (e: Exception) {
                log.warn(e) { "Error loading prev chapter" }
            }
        }
    }

    fun nextChapter() {
        scope.launchDefault {
            val nextChapter = nextChapter.value ?: return@launchDefault
            try {
                _state.value = ReaderChapter.State.Wait
                sendProgress()
                viewerChapters.moveNext()
                initChapters(params.mangaId, nextChapter.chapter.index, fromMenuButton = true)
            } catch (e: Exception) {
                log.warn(e) { "Error loading next chapter" }
            }
        }
    }

    private suspend fun initManga(mangaId: Long) {
        getManga.asFlow(mangaId)
            .take(1)
            .onEach {
                _manga.value = it
            }
            .catch {
                _state.value = ReaderChapter.State.Error(it)
                log.warn(it) { "Error loading manga" }
            }
            .collect()
    }

    private suspend fun initChapters(
        mangaId: Long,
        chapterIndex: Int,
        fromMenuButton: Boolean = true,
    ) {
        //resetValues()
        val (chapter, pages) = coroutineScope {
            val getCurrentChapter = async {
                val chapter = getReaderChapter(chapterIndex) ?: return@async null
                val pages = loader.loadChapter(chapter)
                viewerChapters.currChapter.value = chapter
                chapter to pages
            }

            val getAdjacentChapters = async {
                val chapters = getChapters.await(
                    mangaId,
                    onError = { /* TODO: 2022-07-01 Error toast */ }
                ).orEmpty()

                val nextChapter = async {
                    if (viewerChapters.nextChapter.value == null) {
                        val nextChapter = chapters.find { it.index == chapterIndex + 1 }
                        if (nextChapter != null) {
                            viewerChapters.nextChapter.value = getReaderChapter(nextChapter.index)
                        } else {
                            viewerChapters.nextChapter.value = null
                        }
                    }
                }
                val prevChapter = async {
                    if (viewerChapters.prevChapter.value == null) {
                        val prevChapter = chapters.find { it.index == chapterIndex - 1 }
                        if (prevChapter != null) {
                            viewerChapters.prevChapter.value = getReaderChapter(prevChapter.index)
                        } else {
                            viewerChapters.prevChapter.value = null
                        }
                    }
                }
                nextChapter.await()
                prevChapter.await()
            }

            getAdjacentChapters.await()
            getCurrentChapter.await()
        } ?: return

        chapter.stateObserver
            .onEach {
                _state.value = it
            }
            .launchIn(chapter.scope)
        pages
            .filterIsInstance<PagesState.Success>()
            .onEach { (pageList) ->
                val prevSeparator = ReaderPageSeparator(viewerChapters.prevChapter.value, chapter)
                val nextSeparator = ReaderPageSeparator(chapter, viewerChapters.nextChapter.value)
                _pages.value = (listOf(prevSeparator) + pageList + nextSeparator).toImmutableList()

                if (fromMenuButton) {
                    val lastPageReadOffset = chapter.chapter.meta.juiPageOffset
                    if (lastPageReadOffset != 0) {
                        _currentPageOffset.value = lastPageReadOffset
                    }
                    val lastPageRead = chapter.chapter.lastPageRead
                    _currentPage.value = if (lastPageRead > 0) {
                        pageList[lastPageRead.coerceAtMost(pageList.lastIndex)]
                    } else {
                        pageList.first()
                    }.also { chapter.pageLoader?.loadPage(it) }
                }
            }
            .launchIn(chapter.scope)

        _currentPage
            .filterIsInstance<ReaderPage>()
            .filter { it.chapter.chapter.index == chapterIndex }
            .onEach { page ->
                chapter.pageLoader?.loadPage(page)
                if ((page.index + 1) >= chapter.chapter.pageCount!!) {
                    markChapterRead(chapter)
                }
            }
            .launchIn(chapter.scope)
    }

    private suspend fun getReaderChapter(chapterIndex: Int): ReaderChapter? {
        return ReaderChapter(
            getChapter.asFlow(params.mangaId, chapterIndex)
                .take(1)
                .catch {
                    _state.value = ReaderChapter.State.Error(it)
                    log.warn(it) { "Error getting chapter $chapterIndex" }
                }
                .singleOrNull() ?: return null
        )
    }

    private fun markChapterRead(chapter: ReaderChapter) {
        scope.launch {
            updateChapterRead.await(chapter.chapter, read = true, onError = { toast(it.message.orEmpty()) })
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun sendProgress(
        chapter: Chapter? = this.chapter.value?.chapter,
        lastPageRead: Int = (currentPage.value as? ReaderPage)?.index ?: 0
    ) {
        chapter ?: return
        if (chapter.read) return
        GlobalScope.launch {
            updateChapterLastPageRead.await(
                chapter,
                lastPageRead = lastPageRead,
                onError = { toast(it.message.orEmpty()) }
            )
        }
    }

    fun updateLastPageReadOffset(offset: Int) {
        updateLastPageReadOffset(chapter.value?.chapter ?: return, offset)
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun updateLastPageReadOffset(chapter: Chapter, offset: Int) {
        GlobalScope.launch {
            updateChapterMeta.await(chapter, offset, onError = { toast(it.message.orEmpty()) })
        }
    }

    override fun onDispose() {
        viewerChapters.recycle()
        scope.cancel()
    }

    data class Params(val chapterIndex: Int, val mangaId: Long)

    private companion object {
        private val log = logging()
    }
}
