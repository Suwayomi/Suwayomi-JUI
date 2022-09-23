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

class UpdateChapterBookmarked @Inject constructor(private val chapterRepository: ChapterRepository) {

    suspend fun await(
        mangaId: Long,
        index: Int,
        bookmarked: Boolean,
    ) = asFlow(mangaId, index, bookmarked)
        .catch { log.warn(it) { "Failed to update chapter bookmark for chapter $index of $mangaId" } }
        .collect()

    suspend fun await(
        manga: Manga,
        index: Int,
        bookmarked: Boolean,
    ) = asFlow(manga, index, bookmarked)
        .catch { log.warn(it) { "Failed to update chapter bookmark for chapter $index of ${manga.title}(${manga.id})" } }
        .collect()

    suspend fun await(
        chapter: Chapter,
        bookmarked: Boolean,
    ) = asFlow(chapter, bookmarked)
        .catch { log.warn(it) { "Failed to update chapter bookmark for chapter ${chapter.index} of ${chapter.mangaId}" } }
        .collect()

    fun asFlow(
        mangaId: Long,
        index: Int,
        bookmarked: Boolean,
    ) = chapterRepository.updateChapterBookmarked(
        mangaId = mangaId,
        chapterIndex = index,
        bookmarked = bookmarked,
    )

    fun asFlow(
        manga: Manga,
        index: Int,
        bookmarked: Boolean,
    ) = chapterRepository.updateChapterBookmarked(
        mangaId = manga.id,
        chapterIndex = index,
        bookmarked = bookmarked,
    )

    fun asFlow(
        chapter: Chapter,
        bookmarked: Boolean,
    ) = chapterRepository.updateChapterBookmarked(
        mangaId = chapter.mangaId,
        chapterIndex = chapter.index,
        bookmarked = bookmarked,
    )

    companion object {
        private val log = logging()
    }
}
