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

@Inject
class UpdateChapter(
    private val chapterRepository: ChapterRepository,
    private val serverListeners: ServerListeners,
) {
    suspend fun await(
        chapterId: Long,
        mangaId: Long?,
        bookmarked: Boolean? = null,
        read: Boolean? = null,
        lastPageRead: Int? = null,
        onError: suspend (Throwable) -> Unit = {},
    ) = asFlow(chapterId, mangaId, bookmarked, read, lastPageRead)
        .catch {
            onError(it)
            log.warn(it) { "Failed to update chapter bookmark for chapter $chapterId" }
        }
        .collect()

    suspend fun await(
        chapter: Chapter,
        bookmarked: Boolean? = null,
        read: Boolean? = null,
        lastPageRead: Int? = null,
        onError: suspend (Throwable) -> Unit = {},
    ) = asFlow(chapter, bookmarked, read, lastPageRead)
        .catch {
            onError(it)
            log.warn(it) { "Failed to update chapter bookmark for chapter ${chapter.index} of ${chapter.mangaId}" }
        }
        .collect()

    suspend fun await(
        chapterIds: List<Long>,
        mangaIds: List<Long>?,
        bookmarked: Boolean? = null,
        read: Boolean? = null,
        lastPageRead: Int? = null,
        onError: suspend (Throwable) -> Unit = {},
    ) = asFlow(chapterIds, mangaIds, bookmarked, read, lastPageRead)
        .catch {
            onError(it)
            log.warn(it) { "Failed to update chapter bookmark for chapters $chapterIds" }
        }
        .collect()

    @JvmName("awaitChapters")
    suspend fun await(
        chapters: List<Chapter>,
        bookmarked: Boolean? = null,
        read: Boolean? = null,
        lastPageRead: Int? = null,
        onError: suspend (Throwable) -> Unit = {},
    ) = asFlow(chapters, bookmarked, read, lastPageRead)
        .catch {
            onError(it)
            log.warn(it) { "Failed to update chapter bookmark for chapters ${chapters.joinToString { it.id.toString() }}" }
        }
        .collect()

    fun asFlow(
        chapterId: Long,
        mangaId: Long?,
        bookmarked: Boolean? = null,
        read: Boolean? = null,
        lastPageRead: Int? = null,
    ) = chapterRepository.updateChapter(
        chapterId = chapterId,
        bookmarked = bookmarked,
        read = read,
        lastPageRead = lastPageRead,
    ).onEach { serverListeners.updateManga(mangaId ?: -1) }

    fun asFlow(
        chapter: Chapter,
        bookmarked: Boolean? = null,
        read: Boolean? = null,
        lastPageRead: Int? = null,
    ) = chapterRepository.updateChapter(
        chapterId = chapter.id,
        bookmarked = bookmarked,
        read = read,
        lastPageRead = lastPageRead,
    ).onEach { serverListeners.updateManga(chapter.mangaId) }

    fun asFlow(
        chapterIds: List<Long>,
        mangaIds: List<Long>?,
        bookmarked: Boolean? = null,
        read: Boolean? = null,
        lastPageRead: Int? = null,
    ) = chapterRepository.updateChapters(
        chapterIds = chapterIds,
        bookmarked = bookmarked,
        read = read,
        lastPageRead = lastPageRead,
    ).onEach { serverListeners.updateManga(mangaIds.orEmpty()) }

    @JvmName("asFlowChapters")
    fun asFlow(
        chapters: List<Chapter>,
        bookmarked: Boolean? = null,
        read: Boolean? = null,
        lastPageRead: Int? = null,
    ) = chapterRepository.updateChapters(
        chapterIds = chapters.map { it.id },
        bookmarked = bookmarked,
        read = read,
        lastPageRead = lastPageRead,
    ).onEach { serverListeners.updateManga(chapters.map { it.mangaId }.distinct()) }

    companion object {
        private val log = logging()
    }
}
