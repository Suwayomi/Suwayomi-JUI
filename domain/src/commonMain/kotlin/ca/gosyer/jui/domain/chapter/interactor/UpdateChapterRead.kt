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

class UpdateChapterRead @Inject constructor(private val chapterRepository: ChapterRepository) {

    suspend fun await(
        mangaId: Long,
        index: Int,
        read: Boolean,
    ) = asFlow(mangaId, index, read)
        .catch { log.warn(it) { "Failed to update chapter read status for chapter $index of $mangaId" } }
        .collect()

    suspend fun await(
        manga: Manga,
        index: Int,
        read: Boolean,
    ) = asFlow(manga, index, read)
        .catch { log.warn(it) { "Failed to update chapter read status for chapter $index of ${manga.title}(${manga.id})" } }
        .collect()

    suspend fun await(
        chapter: Chapter,
        read: Boolean,
    ) = asFlow(chapter, read)
        .catch { log.warn(it) { "Failed to update chapter read status for chapter ${chapter.index} of ${chapter.mangaId}" } }
        .collect()

    fun asFlow(
        mangaId: Long,
        index: Int,
        read: Boolean,
    ) = chapterRepository.updateChapterRead(
        mangaId = mangaId,
        chapterIndex = index,
        read = read,
    )

    fun asFlow(
        manga: Manga,
        index: Int,
        read: Boolean,
    ) = chapterRepository.updateChapterRead(
        mangaId = manga.id,
        chapterIndex = index,
        read = read,
    )

    fun asFlow(
        chapter: Chapter,
        read: Boolean,
    ) = chapterRepository.updateChapterRead(
        mangaId = chapter.mangaId,
        chapterIndex = chapter.index,
        read = read,
    )

    companion object {
        private val log = logging()
    }
}
