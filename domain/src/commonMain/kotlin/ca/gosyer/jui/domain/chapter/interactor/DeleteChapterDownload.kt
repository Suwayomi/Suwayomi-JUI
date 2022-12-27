/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.domain.chapter.interactor

import ca.gosyer.jui.domain.ServerListeners
import ca.gosyer.jui.domain.chapter.model.Chapter
import ca.gosyer.jui.domain.chapter.service.ChapterRepository
import ca.gosyer.jui.domain.manga.model.Manga
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import me.tatarka.inject.annotations.Inject
import org.lighthousegames.logging.logging

class DeleteChapterDownload @Inject constructor(
    private val chapterRepository: ChapterRepository,
    private val serverListeners: ServerListeners
) {

    suspend fun await(mangaId: Long, index: Int, onError: suspend (Throwable) -> Unit = {}) = asFlow(mangaId, index)
        .catch {
            onError(it)
            log.warn(it) { "Failed to delete chapter download for $index of $mangaId" }
        }
        .collect()

    suspend fun await(manga: Manga, index: Int, onError: suspend (Throwable) -> Unit = {}) = asFlow(manga, index)
        .catch {
            onError(it)
            log.warn(it) { "Failed to delete chapter download for $index of ${manga.title}(${manga.id})" }
        }
        .collect()

    suspend fun await(chapter: Chapter, onError: suspend (Throwable) -> Unit = {}) = asFlow(chapter)
        .catch {
            onError(it)
            log.warn(it) { "Failed to delete chapter download for ${chapter.index} of ${chapter.mangaId}" }
        }
        .collect()

    fun asFlow(mangaId: Long, index: Int) = chapterRepository.deleteChapterDownload(mangaId, index)
        .onEach { serverListeners.updateChapters(mangaId, index) }

    fun asFlow(manga: Manga, index: Int) = chapterRepository.deleteChapterDownload(manga.id, index)
        .onEach { serverListeners.updateChapters(manga.id, index) }

    fun asFlow(chapter: Chapter) = chapterRepository.deleteChapterDownload(chapter.mangaId, chapter.index)
        .onEach { serverListeners.updateChapters(chapter.mangaId, chapter.index) }

    companion object {
        private val log = logging()
    }
}
