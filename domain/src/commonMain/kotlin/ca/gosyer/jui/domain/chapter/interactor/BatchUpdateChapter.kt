/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.domain.chapter.interactor

import ca.gosyer.jui.domain.ServerListeners
import ca.gosyer.jui.domain.chapter.model.Chapter
import ca.gosyer.jui.domain.chapter.model.ChapterBatchEditInput
import ca.gosyer.jui.domain.chapter.model.ChapterChange
import ca.gosyer.jui.domain.chapter.model.MangaChapterBatchEditInput
import ca.gosyer.jui.domain.chapter.service.ChapterRepository
import ca.gosyer.jui.domain.manga.model.Manga
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import me.tatarka.inject.annotations.Inject
import org.lighthousegames.logging.logging
import kotlin.jvm.JvmName

class BatchUpdateChapter @Inject constructor(
    private val chapterRepository: ChapterRepository,
    private val serverListeners: ServerListeners,
) {

    @JvmName("awaitChapters")
    suspend fun await(
        mangaId: Long,
        chapters: List<Chapter>,
        isRead: Boolean? = null,
        isBookmarked: Boolean? = null,
        lastPageRead: Int? = null,
        delete: Boolean? = null,
        onError: suspend (Throwable) -> Unit = {}
    ) = asFlow(mangaId, chapters, isRead, isBookmarked, lastPageRead, delete)
        .catch {
            onError(it)
            log.warn(it) { "Failed to update multiple chapters of $mangaId" }
        }
        .collect()

    suspend fun await(
        mangaId: Long,
        chapterIds: List<Long>,
        isRead: Boolean? = null,
        isBookmarked: Boolean? = null,
        lastPageRead: Int? = null,
        delete: Boolean? = null,
        onError: suspend (Throwable) -> Unit = {}
    ) = asFlow(mangaId, chapterIds, isRead, isBookmarked, lastPageRead, delete)
        .catch {
            onError(it)
            log.warn(it) { "Failed to update multiple chapters of $mangaId" }
        }
        .collect()

    @JvmName("awaitChapters")
    suspend fun await(
        manga: Manga,
        chapters: List<Chapter>,
        isRead: Boolean? = null,
        isBookmarked: Boolean? = null,
        lastPageRead: Int? = null,
        delete: Boolean? = null,
        onError: suspend (Throwable) -> Unit = {}
    ) = asFlow(manga, chapters, isRead, isBookmarked, lastPageRead, delete)
        .catch {
            onError(it)
            log.warn(it) { "Failed to update multiple chapters of ${manga.title}(${manga.id})" }
        }
        .collect()

    suspend fun await(
        manga: Manga,
        chapterIds: List<Long>,
        isRead: Boolean? = null,
        isBookmarked: Boolean? = null,
        lastPageRead: Int? = null,
        delete: Boolean? = null,
        onError: suspend (Throwable) -> Unit = {}
    ) = asFlow(manga, chapterIds, isRead, isBookmarked, lastPageRead, delete)
        .catch {
            onError(it)
            log.warn(it) { "Failed to update multiple chapters of ${manga.title}(${manga.id})" }
        }
        .collect()

    @JvmName("awaitChapters")
    suspend fun await(
        chapters: List<Chapter>,
        isRead: Boolean? = null,
        isBookmarked: Boolean? = null,
        lastPageRead: Int? = null,
        delete: Boolean? = null,
        onError: suspend (Throwable) -> Unit = {}
    ) = asFlow(chapters, isRead, isBookmarked, lastPageRead, delete)
        .catch {
            onError(it)
            log.warn(it) { "Failed to update multiple chapters" }
        }
        .collect()

    suspend fun await(
        chapterIds: List<Long>,
        isRead: Boolean? = null,
        isBookmarked: Boolean? = null,
        lastPageRead: Int? = null,
        delete: Boolean? = null,
        onError: suspend (Throwable) -> Unit = {}
    ) = asFlow(chapterIds, isRead, isBookmarked, lastPageRead, delete)
        .catch {
            onError(it)
            log.warn(it) { "Failed to update update multiple chapters" }
        }
        .collect()

    @JvmName("asFlowChapters")
    fun asFlow(
        mangaId: Long,
        chapters: List<Chapter>,
        isRead: Boolean? = null,
        isBookmarked: Boolean? = null,
        lastPageRead: Int? = null,
        delete: Boolean? = null
    ) = getFlow(
        mangaId = mangaId,
        chapterIds = chapters.map { it.id },
        isRead = isRead,
        isBookmarked = isBookmarked,
        lastPageRead = lastPageRead,
        delete = delete
    )

    fun asFlow(
        mangaId: Long,
        chapterIds: List<Long>,
        isRead: Boolean? = null,
        isBookmarked: Boolean? = null,
        lastPageRead: Int? = null,
        delete: Boolean? = null
    ) = getFlow(
        mangaId = mangaId,
        chapterIds = chapterIds,
        isRead = isRead,
        isBookmarked = isBookmarked,
        lastPageRead = lastPageRead,
        delete = delete
    )

    @JvmName("asFlowChapters")
    fun asFlow(
        manga: Manga,
        chapters: List<Chapter>,
        isRead: Boolean? = null,
        isBookmarked: Boolean? = null,
        lastPageRead: Int? = null,
        delete: Boolean? = null
    ) = getFlow(
        mangaId = manga.id,
        chapterIds = chapters.map { it.id },
        isRead = isRead,
        isBookmarked = isBookmarked,
        lastPageRead = lastPageRead,
        delete = delete
    )

    fun asFlow(
        manga: Manga,
        chapterIds: List<Long>,
        isRead: Boolean? = null,
        isBookmarked: Boolean? = null,
        lastPageRead: Int? = null,
        delete: Boolean? = null
    ) = getFlow(
        mangaId = manga.id,
        chapterIds = chapterIds,
        isRead = isRead,
        isBookmarked = isBookmarked,
        lastPageRead = lastPageRead,
        delete = delete
    )

    @JvmName("asFlowChapters")
    fun asFlow(
        chapters: List<Chapter>,
        isRead: Boolean? = null,
        isBookmarked: Boolean? = null,
        lastPageRead: Int? = null,
        delete: Boolean? = null
    ) = getFlow(
        mangaId = null,
        chapterIds = chapters.map { it.id },
        isRead = isRead,
        isBookmarked = isBookmarked,
        lastPageRead = lastPageRead,
        delete = delete
    )

    fun asFlow(
        chapterIds: List<Long>,
        isRead: Boolean? = null,
        isBookmarked: Boolean? = null,
        lastPageRead: Int? = null,
        delete: Boolean? = null
    ) = getFlow(
        mangaId = null,
        chapterIds = chapterIds,
        isRead = isRead,
        isBookmarked = isBookmarked,
        lastPageRead = lastPageRead,
        delete = delete
    )

    private fun getFlow(
        mangaId: Long?,
        chapterIds: List<Long>,
        isRead: Boolean? = null,
        isBookmarked: Boolean? = null,
        lastPageRead: Int? = null,
        delete: Boolean? = null
    ) = if (mangaId != null) {
        chapterRepository.batchUpdateChapter(
            mangaId,
            MangaChapterBatchEditInput(
                chapterIds = chapterIds,
                change = ChapterChange(
                    isRead = isRead,
                    isBookmarked = isBookmarked,
                    lastPageRead = lastPageRead,
                    delete = delete
                )
            )
        )
    } else {
        chapterRepository.batchUpdateChapter(
            ChapterBatchEditInput(
                chapterIds = chapterIds,
                change = ChapterChange(
                    isRead = isRead,
                    isBookmarked = isBookmarked,
                    lastPageRead = lastPageRead,
                    delete = delete
                )
            )
        )
    }.onEach {
        serverListeners.updateChapters(mangaId, chapterIds)
    }

    companion object {
        private val log = logging()
    }
}
