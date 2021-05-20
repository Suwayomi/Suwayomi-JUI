/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.reader.loader

import ca.gosyer.data.reader.ReaderPreferences
import ca.gosyer.data.server.interactions.ChapterInteractionHandler
import ca.gosyer.ui.reader.model.ReaderChapter
import ca.gosyer.ui.reader.model.ReaderPage
import ca.gosyer.util.system.CKLogger
import io.github.kerubistan.kroki.coroutines.priorityChannel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicInteger
import kotlin.coroutines.CoroutineContext

class TachideskPageLoader(
    context: CoroutineContext,
    val chapter: ReaderChapter,
    readerPreferences: ReaderPreferences,
    chapterHandler: ChapterInteractionHandler
) : PageLoader() {
    val scope = CoroutineScope(SupervisorJob() + context)

    /**
     * A channel used to manage requests one by one while allowing priorities.
     */
    private val channel = priorityChannel<PriorityPage>()

    /**
     * The amount of pages to preload before stopping
     */
    private val preloadSize = readerPreferences.preload().stateIn(scope)

    /**
     * The pages stateflow
     */
    private val pagesFlow by lazy {
        MutableStateFlow<List<ReaderPage>>(emptyList())
    }

    init {
        repeat(readerPreferences.threads().get()) {
            scope.launch {
                while (true) {
                    try {
                        for (priorityPage in channel) {
                            val page = priorityPage.page
                            debug { "Loading page ${page.index}" }
                            if (page.status.value == ReaderPage.Status.QUEUE) {
                                try {
                                    page.bitmap.value = chapterHandler.getPage(chapter.chapter, page.index)
                                    page.status.value = ReaderPage.Status.READY
                                    page.error.value = null
                                } catch (e: Exception) {
                                    if (e is CancellationException) throw e
                                    page.bitmap.value = null
                                    page.status.value = ReaderPage.Status.ERROR
                                    page.error.value = e.message
                                }
                            }
                        }
                    } catch (e: Exception) {
                        if (e is CancellationException) throw e
                    }
                }
            }
        }
    }

    /**
     * Preloads the given [amount] of pages after the [currentPage] with a lower priority.
     * @return a list of [PriorityPage] that were added to the [channel]
     */
    private fun preloadNextPages(currentPage: ReaderPage, amount: Int): List<PriorityPage> {
        val pageIndex = currentPage.index
        val pages = currentPage.chapter.pages ?: return emptyList()
        if (pageIndex == pages.value.lastIndex) return emptyList()

        return pages.value
            .subList(pageIndex + 1, (pageIndex + 1 + amount).coerceAtMost(pages.value.size))
            .mapNotNull {
                if (it.status.value == ReaderPage.Status.QUEUE) {
                    PriorityPage(it, 0).apply {
                        scope.launch {
                            channel.send(this@apply)
                        }
                    }
                } else null
            }
    }

    override fun getPages(): StateFlow<List<ReaderPage>> {
        scope.launch {
            if (pagesFlow.value.isNotEmpty()) return@launch
            val pageRange = 0..(chapter.chapter.pageCount?.minus(1) ?: 0)
            pagesFlow.value = pageRange.map {
                ReaderPage(
                    it,
                    MutableStateFlow(null),
                    MutableStateFlow(ReaderPage.Status.QUEUE),
                    MutableStateFlow(null)
                )
            }
        }
        return pagesFlow.asStateFlow()
    }

    override fun loadPage(page: ReaderPage) {
        scope.launch {
            // Automatically retry failed pages when subscribed to this page
            if (page.status.value == ReaderPage.Status.ERROR) {
                page.status.value = ReaderPage.Status.QUEUE
            }

            val queuedPages = mutableListOf<PriorityPage>()
            if (page.status.value == ReaderPage.Status.QUEUE) {
                queuedPages += PriorityPage(page, 1).also {
                    scope.launch { channel.send(it) }
                }
            }
            queuedPages += preloadNextPages(page, preloadSize.value)
        }
    }

    /**
     * Retries a page. This method is only called from user interaction on the viewer.
     */
    override fun retryPage(page: ReaderPage) {
        if (page.status.value == ReaderPage.Status.ERROR) {
            page.status.value = ReaderPage.Status.QUEUE
        }
        scope.launch {
            channel.send(PriorityPage(page, 2))
        }
    }

    /**
     * Data class used to keep ordering of pages in order to maintain priority.
     */
    private class PriorityPage(
        val page: ReaderPage,
        val priority: Int
    ) : Comparable<PriorityPage> {
        companion object {
            private val idGenerator = AtomicInteger()
        }

        private val identifier = idGenerator.incrementAndGet()

        override fun compareTo(other: PriorityPage): Int {
            val p = other.priority.compareTo(priority)
            return if (p != 0) p else identifier.compareTo(other.identifier)
        }
    }

    override fun recycle() {
        super.recycle()
        scope.cancel()
        channel.close()
    }

    private companion object : CKLogger({})
}
