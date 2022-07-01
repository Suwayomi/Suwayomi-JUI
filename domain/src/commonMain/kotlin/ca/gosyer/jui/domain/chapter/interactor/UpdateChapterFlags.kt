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

class UpdateChapterFlags @Inject constructor(private val chapterRepository: ChapterRepository) {

    suspend fun await(
        mangaId: Long,
        index: Int,
        read: Boolean? = null,
        bookmarked: Boolean? = null,
        lastPageRead: Int? = null,
        markPreviousRead: Boolean? = null
    ) = asFlow(mangaId, index, read, bookmarked, lastPageRead, markPreviousRead)
        .catch { log.warn(it) { "Failed to update chapter flags for chapter $index of $mangaId" } }
        .collect()

    suspend fun await(
        manga: Manga,
        index: Int,
        read: Boolean? = null,
        bookmarked: Boolean? = null,
        lastPageRead: Int? = null,
        markPreviousRead: Boolean? = null
    ) = asFlow(manga, index, read, bookmarked, lastPageRead, markPreviousRead)
        .catch { log.warn(it) { "Failed to update chapter flags for chapter $index of ${manga.title}(${manga.id})" } }
        .collect()

    suspend fun await(
        chapter: Chapter,
        read: Boolean? = null,
        bookmarked: Boolean? = null,
        lastPageRead: Int? = null,
        markPreviousRead: Boolean? = null
    ) = asFlow(chapter, read, bookmarked, lastPageRead, markPreviousRead)
        .catch { log.warn(it) { "Failed to update chapter flags for chapter ${chapter.index} of ${chapter.mangaId}" } }
        .collect()

    fun asFlow(
        mangaId: Long,
        index: Int,
        read: Boolean? = null,
        bookmarked: Boolean? = null,
        lastPageRead: Int? = null,
        markPreviousRead: Boolean? = null
    ) = chapterRepository.updateChapter(
        mangaId = mangaId,
        chapterIndex = index,
        read = read,
        bookmarked = bookmarked,
        lastPageRead = lastPageRead,
        markPreviousRead = markPreviousRead
    )

    fun asFlow(
        manga: Manga,
        index: Int,
        read: Boolean? = null,
        bookmarked: Boolean? = null,
        lastPageRead: Int? = null,
        markPreviousRead: Boolean? = null
    ) = chapterRepository.updateChapter(
        mangaId = manga.id,
        chapterIndex = index,
        read = read,
        bookmarked = bookmarked,
        lastPageRead = lastPageRead,
        markPreviousRead = markPreviousRead
    )

    fun asFlow(
        chapter: Chapter,
        read: Boolean? = null,
        bookmarked: Boolean? = null,
        lastPageRead: Int? = null,
        markPreviousRead: Boolean? = null
    ) = chapterRepository.updateChapter(
        mangaId = chapter.mangaId,
        chapterIndex = chapter.index,
        read = read,
        bookmarked = bookmarked,
        lastPageRead = lastPageRead,
        markPreviousRead = markPreviousRead
    )

    companion object {
        private val log = logging()
    }
}
