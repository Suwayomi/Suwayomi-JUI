/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.domain.chapter.interactor

import ca.gosyer.jui.domain.chapter.service.ChapterRepository
import io.ktor.client.request.HttpRequestBuilder
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.singleOrNull
import me.tatarka.inject.annotations.Inject
import org.lighthousegames.logging.logging

@Inject
class GetChapterPages(
    private val chapterRepository: ChapterRepository,
) {
    suspend fun await(
        chapterId: Long,
        onError: suspend (Throwable) -> Unit = {},
    ) = asFlow(chapterId)
        .catch {
            onError(it)
            log.warn(it) { "Failed to get pages for $chapterId" }
        }
        .singleOrNull()

    suspend fun await(
        url: String,
        onError: suspend (Throwable) -> Unit = {},
        block: HttpRequestBuilder.() -> Unit,
    ) = asFlow(url, block)
        .catch {
            onError(it)
            log.warn(it) { "Failed to get page $url" }
        }
        .singleOrNull()

    fun asFlow(chapterId: Long) = chapterRepository.getPages(chapterId)

    fun asFlow(
        url: String,
        block: HttpRequestBuilder.() -> Unit,
    ) = chapterRepository.getPage(url, block)

    companion object {
        private val log = logging()
    }
}
