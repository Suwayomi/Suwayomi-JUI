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

class UpdateChapterBookmarked @Inject constructor(
    private val chapterRepository: ChapterRepository,
    private val serverListeners: ServerListeners
) {

    suspend fun await(
        mangaId: Long,
        index: Int,
        bookmarked: Boolean,
        onError: suspend (Throwable) -> Unit = {}
    ) = asFlow(mangaId, index, bookmarked)
        .catch {
            onError(it)
            log.warn(it) { "Failed to update chapter bookmark for chapter $index of $mangaId" }
        }
        .collect()

    suspend fun await(
        manga: Manga,
        index: Int,
        bookmarked: Boolean,
        onError: suspend (Throwable) -> Unit = {}
    ) = asFlow(manga, index, bookmarked)
        .catch {
            onError(it)
            log.warn(it) { "Failed to update chapter bookmark for chapter $index of ${manga.title}(${manga.id})" }
        }
        .collect()

    suspend fun await(
        chapter: Chapter,
        bookmarked: Boolean,
        onError: suspend (Throwable) -> Unit = {}
    ) = asFlow(chapter, bookmarked)
        .catch {
            onError(it)
            log.warn(it) { "Failed to update chapter bookmark for chapter ${chapter.index} of ${chapter.mangaId}" }
        }
        .collect()

    fun asFlow(
        mangaId: Long,
        index: Int,
        bookmarked: Boolean
    ) = chapterRepository.updateChapter(
        mangaId = mangaId,
        chapterIndex = index,
        bookmarked = bookmarked
    ).onEach { serverListeners.updateChapters(mangaId, index) }

    fun asFlow(
        manga: Manga,
        index: Int,
        bookmarked: Boolean
    ) = chapterRepository.updateChapter(
        mangaId = manga.id,
        chapterIndex = index,
        bookmarked = bookmarked
    ).onEach { serverListeners.updateChapters(manga.id, index) }

    fun asFlow(
        chapter: Chapter,
        bookmarked: Boolean
    ) = chapterRepository.updateChapter(
        mangaId = chapter.mangaId,
        chapterIndex = chapter.index,
        bookmarked = bookmarked
    ).onEach { serverListeners.updateChapters(chapter.mangaId, chapter.index) }

    companion object {
        private val log = logging()
    }
}
