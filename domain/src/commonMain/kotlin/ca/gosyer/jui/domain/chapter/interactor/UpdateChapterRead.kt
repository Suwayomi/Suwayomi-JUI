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

class UpdateChapterRead
    @Inject
    constructor(
        private val chapterRepository: ChapterRepository,
        private val serverListeners: ServerListeners,
    ) {
        suspend fun await(
            chapterId: Long,
            read: Boolean,
            onError: suspend (Throwable) -> Unit = {},
        ) = asFlow(chapterId, read)
            .catch {
                onError(it)
                log.warn(it) { "Failed to update chapter read status for chapter $chapterId" }
            }
            .collect()

        suspend fun await(
            chapter: Chapter,
            read: Boolean,
            onError: suspend (Throwable) -> Unit = {},
        ) = asFlow(chapter, read)
            .catch {
                onError(it)
                log.warn(it) { "Failed to update chapter read status for chapter ${chapter.index} of ${chapter.mangaId}" }
            }
            .collect()

        fun asFlow(
            chapterId: Long,
            read: Boolean,
        ) = chapterRepository.updateChapter(
            chapterId = chapterId,
            read = read,
        ).onEach { serverListeners.updateChapters(chapterId) }

        fun asFlow(
            chapterIds: List<Long>,
            read: Boolean,
        ) = chapterRepository.updateChapters(
            chapterIds = chapterIds,
            read = read,
        ).onEach { serverListeners.updateChapters(chapterIds) }

        fun asFlow(
            chapter: Chapter,
            read: Boolean,
        ) = chapterRepository.updateChapter(
            chapterId = chapter.id,
            read = read,
        ).onEach { serverListeners.updateChapters(chapter.id) }

        companion object {
            private val log = logging()
        }
    }
