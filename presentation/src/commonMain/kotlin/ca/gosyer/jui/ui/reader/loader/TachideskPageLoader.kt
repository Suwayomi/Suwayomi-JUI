/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.reader.loader

import androidx.compose.ui.unit.IntSize
import ca.gosyer.jui.core.io.SYSTEM
import ca.gosyer.jui.core.io.source
import ca.gosyer.jui.core.lang.PriorityChannel
import ca.gosyer.jui.core.lang.throwIfCancellation
import ca.gosyer.jui.domain.chapter.interactor.GetChapterPages
import ca.gosyer.jui.domain.reader.service.ReaderPreferences
import ca.gosyer.jui.domain.server.Http
import ca.gosyer.jui.ui.base.image.BitmapDecoderFactory
import ca.gosyer.jui.ui.base.model.StableHolder
import ca.gosyer.jui.ui.reader.model.ReaderChapter
import ca.gosyer.jui.ui.reader.model.ReaderPage
import cafe.adriel.voyager.core.concurrent.AtomicInt32
import com.seiko.imageloader.asImageBitmap
import com.seiko.imageloader.cache.disk.DiskCache
import com.seiko.imageloader.component.decoder.DecodeResult
import com.seiko.imageloader.model.ImageResult
import com.seiko.imageloader.model.ImageSource
import com.seiko.imageloader.model.ImageSourceFrom
import com.seiko.imageloader.option.Options
import io.ktor.client.plugins.onDownload
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import okio.BufferedSource
import okio.FileSystem
import okio.buffer
import okio.use
import com.diamondedge.logging.logging

class TachideskPageLoader(
    val chapter: ReaderChapter,
    readerPreferences: ReaderPreferences,
    private val http: Http,
    private val chapterCache: DiskCache,
    private val bitmapDecoderFactory: BitmapDecoderFactory,
    private val getChapterPages: GetChapterPages,
) : PageLoader() {
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    /**
     * A channel used to manage requests one by one while allowing priorities.
     */
    private val channel = PriorityChannel<PriorityPage>(
        scope = scope,
        comparator = { i1, i2 -> i1.compareTo(i2) },
    )

    /**
     * The amount of pages to preload before stopping
     */
    private val preloadSize = readerPreferences.preload().stateIn(scope)

    /**
     * The pages stateflow
     */
    private val pagesFlow by lazy {
        MutableStateFlow<PagesState>(PagesState.Loading)
    }

    init {
        readerPreferences.threads()
            .stateIn(scope)
            .mapLatest {
                coroutineScope {
                    repeat(it) {
                        launch {
                            while (true) {
                                try {
                                    for (priorityPage in channel) {
                                        val page = priorityPage.page
                                        if (page.status.value == ReaderPage.Status.QUEUE) {
                                            page.status.value = ReaderPage.Status.WORKING
                                            fetchImage(page)
                                        }
                                    }
                                } catch (e: Exception) {
                                    e.throwIfCancellation()
                                    log.warn(e) { "Error in loop" }
                                }
                            }
                        }
                    }
                }
            }
            .launchIn(scope)
    }

    private suspend fun fetchImage(page: ReaderPage) {
        log.debug { "Loading page ${page.index2}" }
        flow {
            val response = getChapterPages.asFlow(page.url) {
                onDownload { bytesSentTotal, contentLength ->
                    page.progress.value = (bytesSentTotal.toFloat() / (contentLength ?: Long.MAX_VALUE)).coerceAtMost(1.0F)
                }
            }

            emitAll(response)
        }
            .onEach {
                putImageInCache(it, page)
                page.bitmap.value = StableHolder { getImageFromCache(page) }
                page.status.value = ReaderPage.Status.READY
                page.error.value = null
            }
            .catch {
                page.bitmap.value = StableHolder(null)
                page.status.value = ReaderPage.Status.ERROR
                page.error.value = it.message
                log.warn(it) { "Failed to get page ${page.index2} for chapter ${chapter.chapter.index} for ${chapter.chapter.mangaId}" }
            }
            .flowOn(Dispatchers.IO)
            .collect()
    }

    private suspend fun putImageInCache(
        response: ByteArray,
        page: ReaderPage,
    ) {
        val editor = chapterCache.openEditor(page.cacheKey)
            ?: throw Exception("Couldn't open cache")
        try {
            FileSystem.SYSTEM.write(editor.data) {
                response.source().use {
                    writeAll(it)
                }
            }
            editor.commit()
        } catch (e: Exception) {
            editor.abortQuietly()
            throw e
        }
    }

    private suspend fun getImageFromCache(page: ReaderPage): ReaderPage.ImageDecodeState =
        chapterCache.openSnapshot(page.cacheKey)?.use {
            it.source().use { source ->
                val decoder = bitmapDecoderFactory.create(
                    ImageResult.OfSource(
                        ImageSource(source),
                        ImageSourceFrom.Disk,
                    ),
                    Options(),
                )
                if (decoder != null) {
                    runCatching { decoder.decode() as DecodeResult.OfBitmap }
                        .mapCatching {
                            ReaderPage.ImageDecodeState.Success(
                                it.bitmap.asImageBitmap().also {
                                    page.bitmapInfo.value = ReaderPage.BitmapInfo(
                                        IntSize(it.width, it.height),
                                    )
                                },
                            )
                        }
                        .getOrElse {
                            ReaderPage.ImageDecodeState.FailedToDecode(it)
                        }
                } else {
                    ReaderPage.ImageDecodeState.UnknownDecoder
                }
            }
        } ?: ReaderPage.ImageDecodeState.FailedToGetSnapShot

    /**
     * Preloads the given [amount] of pages after the [currentPage] with a lower priority.
     * @return a list of [PriorityPage] that were added to the [channel]
     */
    private fun preloadNextPages(
        currentPage: ReaderPage,
        amount: Int,
    ): List<PriorityPage> {
        val pages = (currentPage.chapter.pages.value as? PagesState.Success)?.pages ?: return emptyList()
        val pageIndex = pages.indexOf(currentPage)
        if (pageIndex >= pages.lastIndex) return emptyList()

        return pages
            .subList(pageIndex + 1, (pageIndex + 1 + amount).coerceAtMost(pages.size))
            .mapNotNull {
                if (it.status.value == ReaderPage.Status.QUEUE) {
                    PriorityPage(it, 0).also {
                        scope.launch {
                            channel.send(it)
                        }
                    }
                } else {
                    null
                }
            }
    }

    override fun getPages(): StateFlow<PagesState> {
        scope.launch {
            if (pagesFlow.value != PagesState.Loading) return@launch
            val pages = getChapterPages.await(chapter.chapter.id)
            pagesFlow.value = if (pages.isNullOrEmpty()) {
                PagesState.Empty
            } else {
                PagesState.Success(
                    pages.mapIndexed { index, url ->
                        ReaderPage(
                            url = url,
                            index2 = index,
                            bitmap = MutableStateFlow(StableHolder(null)),
                            bitmapInfo = MutableStateFlow(null),
                            progress = MutableStateFlow(0.0F),
                            status = MutableStateFlow(ReaderPage.Status.QUEUE),
                            error = MutableStateFlow(null),
                            chapter = chapter,
                        )
                    },
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
        val priority: Int,
    ) : Comparable<PriorityPage> {
        companion object {
            private val idGenerator = AtomicInt32(1)
        }

        private val identifier = idGenerator.getAndIncrement()

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

    private companion object {
        private val log = logging()
    }

    private val ReaderPage.cacheKey
        get() = "${chapter.chapter.id}-$url"

    private fun DiskCache.Snapshot.source(): BufferedSource = FileSystem.SYSTEM.source(data).buffer()

    private fun DiskCache.Editor.abortQuietly() {
        try {
            abort()
        } catch (_: Exception) {
        }
    }
}
