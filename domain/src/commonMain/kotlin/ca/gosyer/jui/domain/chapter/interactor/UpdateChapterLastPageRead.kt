/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.domain.chapter.interactor

import ca.gosyer.jui.domain.ServerListeners
import ca.gosyer.jui.domain.chapter.model.Chapter
import ca.gosyer.jui.domain.chapter.service.ChapterRepository
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import me.tatarka.inject.annotations.Inject
import org.lighthousegames.logging.logging

@Inject
class UpdateChapterLastPageRead(
    private val chapterRepository: ChapterRepository,
    private val serverListeners: ServerListeners,
) {
    suspend fun await(
        chapterId: Long,
        mangaId: Long?,
        lastPageRead: Int,
        onError: suspend (Throwable) -> Unit = {},
    ) = asFlow(chapterId, mangaId, lastPageRead)
        .catch {
            onError(it)
            log.warn(it) { "Failed to update chapter last page read for chapter $chapterId" }
        }
        .collect()

    suspend fun await(
        chapter: Chapter,
        lastPageRead: Int,
        onError: suspend (Throwable) -> Unit = {},
    ) = asFlow(chapter, lastPageRead)
        .catch {
            onError(it)
            log.warn(it) { "Failed to update chapter last page read for chapter ${chapter.index} of ${chapter.mangaId}" }
        }
        .collect()

    fun asFlow(
        chapterId: Long,
        mangaId: Long?,
        lastPageRead: Int,
    ) = chapterRepository.updateChapter(
        chapterId = chapterId,
        lastPageRead = lastPageRead,
    ).onEach { serverListeners.updateManga(mangaId ?: -1) }

    fun asFlow(
        chapter: Chapter,
        lastPageRead: Int,
    ) = chapterRepository.updateChapter(
        chapterId = chapter.id,
        lastPageRead = lastPageRead,
    ).onEach { serverListeners.updateManga(chapter.mangaId) }

    companion object {
        private val log = logging()
    }
}
