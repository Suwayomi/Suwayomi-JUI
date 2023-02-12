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
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.singleOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Assisted
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
    @Assisted private val params: Params
) : ViewModel(contextWrapper) {
    override val scope = MainScope()
    private val _manga = MutableStateFlow<Manga?>(null)
    private val viewerChapters = MutableStateFlow(ViewerChapters(null, null, null))
    val previousChapter = viewerChapters.map { it.prevChapter }.stateIn(scope, SharingStarted.Eagerly, null)
    val chapter = viewerChapters.map { it.currChapter }.stateIn(scope, SharingStarted.Eagerly, null)
    val nextChapter = viewerChapters.map { it.nextChapter }.stateIn(scope, SharingStarted.Eagerly, null)

    private val _state = MutableStateFlow<ReaderChapter.State>(ReaderChapter.State.Wait)
    val state = _state.asStateFlow()

    val pages = viewerChapters.flatMapLatest { viewerChapters ->
        val previousChapterPages = viewerChapters.prevChapter
            ?.pages
            ?.map { (it as? PagesState.Success)?.pages }
            ?: flowOf(null)
        val chapterPages = viewerChapters.currChapter
            ?.pages
            ?.map { (it as? PagesState.Success)?.pages }
            ?: flowOf(null)
        val nextChapterPages = viewerChapters.nextChapter
            ?.pages
            ?.map { (it as? PagesState.Success)?.pages }
            ?: flowOf(null)
        combine(previousChapterPages, chapterPages, nextChapterPages) { prev, cur, next ->
                (
                    prev.orEmpty() +
                        ReaderPageSeparator(viewerChapters.prevChapter, viewerChapters.currChapter) +
                        cur.orEmpty() +
                        ReaderPageSeparator(viewerChapters.currChapter, viewerChapters.nextChapter) +
                        next.orEmpty()
                    ).toImmutableList()

        }
    }.stateIn(scope, SharingStarted.Eagerly, persistentListOf())

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

    init {
        scope.launchDefault {
            currentPage
                .filterIsInstance<ReaderPage>()
                .collectLatest { page ->
                    page.chapter.pageLoader?.loadPage(page)
                    if (page.chapter == chapter.value) {
                        if ((page.index + 1) >= page.chapter.chapter.pageCount!!) {
                            markChapterRead(page.chapter)
                        }
                        val nextChapter = nextChapter.value
                        if (nextChapter != null && (page.index + 1) >= (page.chapter.chapter.pageCount!! - 5)) {
                            requestPreloadChapter(nextChapter)
                        }
                    } else {
                        val previousChapter = previousChapter.value
                        val nextChapter = nextChapter.value
                        if (page.chapter == previousChapter) {
                            viewerChapters.value = viewerChapters.value.movePrev()
                            initChapters(params.mangaId, page.chapter.chapter.index, fromMenuButton = false)
                        } else if (page.chapter == nextChapter) {
                            viewerChapters.value = viewerChapters.value.moveNext()
                            initChapters(params.mangaId, page.chapter.chapter.index, fromMenuButton = false)
                        }
                    }
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
                viewerChapters.value = viewerChapters.value.movePrev()
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
                viewerChapters.value = viewerChapters.value.moveNext()
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
        fromMenuButton: Boolean = true
    ) {
        log.debug { "Loading chapter index $chapterIndex" }
        val (chapter, pages) = coroutineScope {
            val getCurrentChapter = async {
                val chapter = getReaderChapter(chapterIndex) ?: return@async null
                val pages = loader.loadChapter(chapter)
                viewerChapters.update { it.copy(currChapter = chapter) }
                chapter to pages
            }

            val getAdjacentChapters = async {
                val chapters = getChapters.await(
                    mangaId,
                    onError = { /* TODO: 2022-07-01 Error toast */ }
                ).orEmpty()

                val nextChapter = async {
                    if (viewerChapters.value.nextChapter == null) {
                        val nextChapter = chapters.find { it.index == chapterIndex + 1 }
                        if (nextChapter != null) {
                            val nextReaderChapter = getReaderChapter(nextChapter.index)
                            viewerChapters.update { it.copy(nextChapter = nextReaderChapter) }
                        } else {
                            viewerChapters.update { it.copy(nextChapter = null) }
                        }
                    }
                }
                val prevChapter = async {
                    if (viewerChapters.value.prevChapter == null) {
                        val prevChapter = chapters.find { it.index == chapterIndex - 1 }
                        if (prevChapter != null) {
                            val prevReaderChapter = getReaderChapter(prevChapter.index)
                            viewerChapters.update { it.copy(prevChapter = prevReaderChapter) }
                        } else {
                            viewerChapters.update { it.copy(prevChapter = null) }
                        }
                    }
                }
                nextChapter.await()
                prevChapter.await()
            }

            getAdjacentChapters.await()
            getCurrentChapter.await()
        } ?: return

        if (fromMenuButton) {
            chapter.stateObserver
                .onEach {
                    _state.value = it
                }
                .launchIn(chapter.scope)

            pages
                .filterIsInstance<PagesState.Success>()
                .onEach { (pageList) ->
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
                .launchIn(chapter.scope)
        }
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

    fun requestPreloadChapter(chapter: ReaderChapter) {
        if (chapter.state != ReaderChapter.State.Wait && chapter.state !is ReaderChapter.State.Error) {
            return
        }
        log.debug { "Preloading ${chapter.chapter.index}" }
        loader.loadChapter(chapter)
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
        viewerChapters.value.recycle()
        scope.cancel()
    }

    data class Params(val chapterIndex: Int, val mangaId: Long)

    private companion object {
        private val log = logging()
    }
}
