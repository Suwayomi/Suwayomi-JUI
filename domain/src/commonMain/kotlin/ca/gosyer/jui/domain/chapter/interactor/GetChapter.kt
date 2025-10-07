/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.domain.chapter.interactor

import ca.gosyer.jui.domain.ServerListeners
import ca.gosyer.jui.domain.chapter.model.Chapter
import ca.gosyer.jui.domain.chapter.service.ChapterRepository
import com.diamondedge.logging.logging
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.singleOrNull
import kotlinx.coroutines.flow.take
import me.tatarka.inject.annotations.Inject

@Inject
class GetChapter(
    private val chapterRepository: ChapterRepository,
    private val serverListeners: ServerListeners,
) {
    suspend fun await(
        chapterId: Long,
        mangaId: Long?,
        onError: suspend (Throwable) -> Unit = {},
    ) = asFlow(chapterId, mangaId)
        .take(1)
        .catch {
            onError(it)
            log.warn(it) { "Failed to get chapter $chapterId" }
        }
        .singleOrNull()

    suspend fun await(
        chapter: Chapter,
        onError: suspend (Throwable) -> Unit = {},
    ) = asFlow(chapter)
        .take(1)
        .catch {
            onError(it)
            log.warn(it) { "Failed to get chapter ${chapter.index} for ${chapter.mangaId}" }
        }
        .singleOrNull()

    fun asFlow(chapterId: Long, mangaId: Long?) =
        serverListeners.combineMangaUpdates(
            chapterRepository.getChapter(chapterId),
            predate = { ids -> mangaId in ids },
        )

    fun asFlow(chapter: Chapter) =
        serverListeners.combineMangaUpdates(
            chapterRepository.getChapter(chapter.id),
            predate = { ids -> chapter.mangaId in ids },
        )

    companion object {
        private val log = logging()
    }
}
