/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.domain.chapter.interactor

import ca.gosyer.jui.domain.ServerListeners
import ca.gosyer.jui.domain.chapter.service.ChapterRepository
import ca.gosyer.jui.domain.manga.model.Manga
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.singleOrNull
import me.tatarka.inject.annotations.Inject
import org.lighthousegames.logging.logging

@Inject
class RefreshChapters(
    private val chapterRepository: ChapterRepository,
    private val serverListeners: ServerListeners,
) {
    suspend fun await(
        mangaId: Long,
        onError: suspend (Throwable) -> Unit = {},
    ) = asFlow(mangaId)
        .catch {
            onError(it)
            log.warn(it) { "Failed to refresh chapters for $mangaId" }
        }
        .singleOrNull()

    suspend fun await(
        manga: Manga,
        onError: suspend (Throwable) -> Unit = {},
    ) = asFlow(manga)
        .catch {
            onError(it)
            log.warn(it) { "Failed to refresh chapters for ${manga.title}(${manga.id})" }
        }
        .singleOrNull()

    fun asFlow(mangaId: Long, ) =
        chapterRepository.fetchChapters(mangaId)
            .onEach { serverListeners.updateManga(mangaId) }

    fun asFlow(manga: Manga) =
        chapterRepository.fetchChapters(manga.id)
            .onEach { serverListeners.updateManga(manga.id) }

    companion object {
        private val log = logging()
    }
}
