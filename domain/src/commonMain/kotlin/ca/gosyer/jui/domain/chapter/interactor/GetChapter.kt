/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.domain.chapter.interactor

import ca.gosyer.jui.domain.chapter.model.Chapter
import ca.gosyer.jui.domain.chapter.service.ChapterRepository
import ca.gosyer.jui.domain.manga.model.Manga
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.singleOrNull
import me.tatarka.inject.annotations.Inject
import org.lighthousegames.logging.logging

class GetChapter @Inject constructor(private val chapterRepository: ChapterRepository) {

    suspend fun await(mangaId: Long, index: Int, onError: suspend (Throwable) -> Unit = {}) = asFlow(mangaId, index)
        .catch {
            onError(it)
            log.warn(it) { "Failed to get chapter $index for $mangaId" }
        }
        .singleOrNull()

    suspend fun await(manga: Manga, index: Int, onError: suspend (Throwable) -> Unit = {}) = asFlow(manga, index)
        .catch {
            onError(it)
            log.warn(it) { "Failed to get chapter $index for ${manga.title}(${manga.id})" }
        }
        .singleOrNull()

    suspend fun await(chapter: Chapter, onError: suspend (Throwable) -> Unit = {}) = asFlow(chapter)
        .catch {
            onError(it)
            log.warn(it) { "Failed to get chapter ${chapter.index} for ${chapter.mangaId}" }
        }
        .singleOrNull()

    fun asFlow(mangaId: Long, index: Int) = chapterRepository.getChapter(mangaId, index)

    fun asFlow(manga: Manga, index: Int) = chapterRepository.getChapter(manga.id, index)

    fun asFlow(chapter: Chapter) = chapterRepository.getChapter(chapter.mangaId, chapter.index)

    companion object {
        private val log = logging()
    }
}
