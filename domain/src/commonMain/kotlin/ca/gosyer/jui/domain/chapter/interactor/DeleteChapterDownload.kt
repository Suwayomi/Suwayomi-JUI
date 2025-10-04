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
import kotlin.jvm.JvmName

class DeleteChapterDownload
    @Inject
    constructor(
        private val chapterRepository: ChapterRepository,
        private val serverListeners: ServerListeners,
    ) {
        suspend fun await(
            chapterId: Long,
            onError: suspend (Throwable) -> Unit = {},
        ) = asFlow(chapterId)
            .catch {
                onError(it)
                log.warn(it) { "Failed to delete chapter download for $chapterId" }
            }
            .collect()

        @JvmName("awaitChapter")
        suspend fun await(
            chapter: Chapter,
            onError: suspend (Throwable) -> Unit = {},
        ) = asFlow(chapter)
            .catch {
                onError(it)
                log.warn(it) { "Failed to delete chapter download for ${chapter.index} of ${chapter.mangaId}" }
            }
            .collect()

        suspend fun await(
            chapterIds: List<Long>,
            onError: suspend (Throwable) -> Unit = {},
        ) = asFlow(chapterIds)
            .catch {
                onError(it)
                log.warn(it) { "Failed to delete chapter download for $chapterIds" }
            }
            .collect()

        @JvmName("awaitChapters")
        suspend fun await(
            chapters: List<Chapter>,
            onError: suspend (Throwable) -> Unit = {},
        ) = asFlow(chapters)
            .catch {
                onError(it)
                log.warn(it) { "Failed to delete chapter download for ${chapters.joinToString { it.id.toString() }}" }
            }
            .collect()

        fun asFlow(chapterId: Long) =
            chapterRepository.deleteDownloadedChapter(chapterId)
                .onEach { serverListeners.updateChapters(chapterId) }

        @JvmName("asFlowChapter")
        fun asFlow(chapter: Chapter) =
            chapterRepository.deleteDownloadedChapter(chapter.id)
                .onEach { serverListeners.updateChapters(chapter.id) }

        fun asFlow(chapterIds: List<Long>) =
            chapterRepository.deleteDownloadedChapters(chapterIds)
                .onEach { serverListeners.updateChapters(chapterIds) }

        @JvmName("asFlowChapters")
        fun asFlow(chapter: List<Chapter>) =
            chapterRepository.deleteDownloadedChapters(chapter.map { it.id })
                .onEach { serverListeners.updateChapters(chapter.map { it.id }) }

        companion object {
            private val log = logging()
        }
    }
