/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.reader

import ca.gosyer.jui.domain.chapter.interactor.GetChapterPages
import ca.gosyer.jui.domain.reader.service.ReaderPreferences
import ca.gosyer.jui.domain.server.Http
import ca.gosyer.jui.ui.base.image.BitmapDecoderFactory
import ca.gosyer.jui.ui.reader.loader.PagesState
import ca.gosyer.jui.ui.reader.loader.TachideskPageLoader
import ca.gosyer.jui.ui.reader.model.ReaderChapter
import com.seiko.imageloader.cache.disk.DiskCache
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.dropWhile
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take
import org.lighthousegames.logging.logging

class ChapterLoader(
    private val readerPreferences: ReaderPreferences,
    private val http: Http,
    private val chapterCache: DiskCache,
    private val bitmapDecoderFactory: BitmapDecoderFactory,
    private val getChapterPages: GetChapterPages,
) {
    fun loadChapter(chapter: ReaderChapter): StateFlow<PagesState> {
        if (chapterIsReady(chapter)) {
            return (chapter.state as ReaderChapter.State.Loaded).pages
        } else {
            chapter.state = ReaderChapter.State.Loading
            log.debug { "Loading pages for ${chapter.chapter.name}" }

            val loader = TachideskPageLoader(chapter, readerPreferences, http, chapterCache, bitmapDecoderFactory, getChapterPages)

            val pages = loader.getPages()

            pages
                .dropWhile { it is PagesState.Loading }
                .take(1)
                .filterIsInstance<PagesState.Empty>()
                .onEach {
                    chapter.state = ReaderChapter.State.Error(Exception("No pages found"))
                }
                .launchIn(chapter.scope)

            chapter.pageLoader = loader // Assign here to fix race with unref
            chapter.state = ReaderChapter.State.Loaded(pages)
            return pages
        }
    }

    /**
     * Checks [chapter] to be loaded based on present pages and loader in addition to state.
     */
    private fun chapterIsReady(chapter: ReaderChapter): Boolean = chapter.state is ReaderChapter.State.Loaded && chapter.pageLoader != null

    private companion object {
        private val log = logging()
    }
}
