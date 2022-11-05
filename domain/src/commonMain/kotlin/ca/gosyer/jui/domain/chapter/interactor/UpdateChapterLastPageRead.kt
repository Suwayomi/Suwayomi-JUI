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
import kotlinx.coroutines.flow.collect
import me.tatarka.inject.annotations.Inject
import org.lighthousegames.logging.logging

class UpdateChapterLastPageRead @Inject constructor(private val chapterRepository: ChapterRepository) {

    suspend fun await(
        mangaId: Long,
        index: Int,
        lastPageRead: Int,
        onError: suspend (Throwable) -> Unit = {}
    ) = asFlow(mangaId, index, lastPageRead)
        .catch {
            onError(it)
            log.warn(it) { "Failed to update chapter last page read for chapter $index of $mangaId" }
        }
        .collect()

    suspend fun await(
        manga: Manga,
        index: Int,
        lastPageRead: Int,
        onError: suspend (Throwable) -> Unit = {}
    ) = asFlow(manga, index, lastPageRead)
        .catch {
            onError(it)
            log.warn(it) { "Failed to update chapter last page read for chapter $index of ${manga.title}(${manga.id})" }
        }
        .collect()

    suspend fun await(
        chapter: Chapter,
        lastPageRead: Int,
        onError: suspend (Throwable) -> Unit = {}
    ) = asFlow(chapter, lastPageRead)
        .catch {
            onError(it)
            log.warn(it) { "Failed to update chapter last page read for chapter ${chapter.index} of ${chapter.mangaId}" }
        }
        .collect()

    fun asFlow(
        mangaId: Long,
        index: Int,
        lastPageRead: Int
    ) = chapterRepository.updateChapterLastPageRead(
        mangaId = mangaId,
        chapterIndex = index,
        lastPageRead = lastPageRead
    )

    fun asFlow(
        manga: Manga,
        index: Int,
        lastPageRead: Int
    ) = chapterRepository.updateChapterLastPageRead(
        mangaId = manga.id,
        chapterIndex = index,
        lastPageRead = lastPageRead
    )

    fun asFlow(
        chapter: Chapter,
        lastPageRead: Int
    ) = chapterRepository.updateChapterLastPageRead(
        mangaId = chapter.mangaId,
        chapterIndex = chapter.index,
        lastPageRead = lastPageRead
    )

    companion object {
        private val log = logging()
    }
}
