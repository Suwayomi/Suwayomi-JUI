/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.reader

import ca.gosyer.data.reader.ReaderPreferences
import ca.gosyer.data.server.interactions.ChapterInteractionHandler
import ca.gosyer.ui.reader.loader.TachideskPageLoader
import ca.gosyer.ui.reader.model.ReaderChapter
import ca.gosyer.ui.reader.model.ReaderPage
import ca.gosyer.util.system.CKLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take
import kotlin.coroutines.CoroutineContext

class ChapterLoader(
    val context: CoroutineContext,
    private val readerPreferences: ReaderPreferences,
    private val chapterHandler: ChapterInteractionHandler
) {
    fun loadChapter(chapter: ReaderChapter): StateFlow<List<ReaderPage>> {
        if (chapterIsReady(chapter)) {
            return (chapter.state as ReaderChapter.State.Loaded).pages
        } else {
            chapter.state = ReaderChapter.State.Loading
            debug { "Loading pages for ${chapter.chapter.name}" }

            val loader = TachideskPageLoader(context + Dispatchers.Default, chapter, readerPreferences, chapterHandler)

            val pages = loader.getPages()

            pages.drop(1).take(1).onEach { newPages ->
                if (newPages.isEmpty()) {
                    chapter.state = ReaderChapter.State.Error(Exception("No pages found"))
                }
            }.launchIn(chapter.scope)

            chapter.pageLoader = loader // Assign here to fix race with unref
            chapter.state = ReaderChapter.State.Loaded(pages)
            return pages
        }
    }

    /**
     * Checks [chapter] to be loaded based on present pages and loader in addition to state.
     */
    private fun chapterIsReady(chapter: ReaderChapter): Boolean {
        return chapter.state is ReaderChapter.State.Loaded && chapter.pageLoader != null
    }

    private companion object : CKLogger({})
}
