/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.domain.chapter.interactor

import ca.gosyer.jui.domain.chapter.model.Chapter
import ca.gosyer.jui.domain.chapter.service.ChapterRepository
import ca.gosyer.jui.domain.manga.model.Manga
import io.ktor.client.request.HttpRequestBuilder
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.singleOrNull
import me.tatarka.inject.annotations.Inject
import org.lighthousegames.logging.logging

class GetChapterPage
    @Inject
    constructor(private val chapterRepository: ChapterRepository) {
        suspend fun await(
            mangaId: Long,
            index: Int,
            pageNum: Int,
            block: HttpRequestBuilder.() -> Unit,
            onError: suspend (Throwable) -> Unit = {},
        ) = asFlow(mangaId, index, pageNum, block)
            .catch {
                onError(it)
                log.warn(it) { "Failed to get page $pageNum for chapter $index for $mangaId" }
            }
            .singleOrNull()

        suspend fun await(
            manga: Manga,
            index: Int,
            pageNum: Int,
            block: HttpRequestBuilder.() -> Unit,
            onError: suspend (Throwable) -> Unit = {},
        ) = asFlow(manga, index, pageNum, block)
            .catch {
                onError(it)
                log.warn(it) { "Failed to get page $pageNum for chapter $index for ${manga.title}(${manga.id})" }
            }
            .singleOrNull()

        suspend fun await(
            chapter: Chapter,
            pageNum: Int,
            block: HttpRequestBuilder.() -> Unit,
            onError: suspend (Throwable) -> Unit = {},
        ) = asFlow(chapter, pageNum, block)
            .catch {
                onError(it)
                log.warn(it) { "Failed to get page $pageNum for chapter ${chapter.index} for ${chapter.mangaId}" }
            }
            .singleOrNull()

        fun asFlow(
            mangaId: Long,
            index: Int,
            pageNum: Int,
            block: HttpRequestBuilder.() -> Unit,
        ) = chapterRepository.getPage(mangaId, index, pageNum, block)

        fun asFlow(
            manga: Manga,
            index: Int,
            pageNum: Int,
            block: HttpRequestBuilder.() -> Unit,
        ) = chapterRepository.getPage(manga.id, index, pageNum, block)

        fun asFlow(
            chapter: Chapter,
            pageNum: Int,
            block: HttpRequestBuilder.() -> Unit,
        ) = chapterRepository.getPage(chapter.mangaId, chapter.index, pageNum, block)

        companion object {
            private val log = logging()
        }
    }
