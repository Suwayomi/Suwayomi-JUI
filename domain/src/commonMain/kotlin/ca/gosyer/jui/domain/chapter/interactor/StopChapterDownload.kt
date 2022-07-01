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

class StopChapterDownload @Inject constructor(private val chapterRepository: ChapterRepository) {

    suspend fun await(mangaId: Long, index: Int) = asFlow(mangaId, index)
        .catch { log.warn(it) { "Failed to stop chapter download for $index of $mangaId" } }
        .collect()

    suspend fun await(manga: Manga, index: Int) = asFlow(manga, index)
        .catch { log.warn(it) { "Failed to stop chapter download for $index of ${manga.title}(${manga.id})" } }
        .collect()

    suspend fun await(chapter: Chapter) = asFlow(chapter)
        .catch { log.warn(it) { "Failed to stop chapter download for ${chapter.index} of ${chapter.mangaId}" } }
        .collect()

    fun asFlow(mangaId: Long, index: Int) = chapterRepository.stopChapterDownload(mangaId, index)

    fun asFlow(manga: Manga, index: Int) = chapterRepository.stopChapterDownload(manga.id, index)

    fun asFlow(chapter: Chapter) = chapterRepository.stopChapterDownload(chapter.mangaId, chapter.index)

    companion object {
        private val log = logging()
    }
}
