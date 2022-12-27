/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.domain.manga.interactor

import ca.gosyer.jui.domain.ServerListeners
import ca.gosyer.jui.domain.manga.model.Manga
import ca.gosyer.jui.domain.manga.service.MangaRepository
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.singleOrNull
import kotlinx.coroutines.flow.take
import me.tatarka.inject.annotations.Inject
import org.lighthousegames.logging.logging

class GetMangaFull @Inject constructor(
    private val mangaRepository: MangaRepository,
    private val serverListeners: ServerListeners,
) {

    suspend fun await(mangaId: Long, onError: suspend (Throwable) -> Unit = {}) = asFlow(mangaId)
        .take(1)
        .catch {
            onError(it)
            log.warn(it) { "Failed to get full manga $mangaId" }
        }
        .singleOrNull()

    suspend fun await(manga: Manga, onError: suspend (Throwable) -> Unit = {}) = asFlow(manga)
        .take(1)
        .catch {
            onError(it)
            log.warn(it) { "Failed to get full manga ${manga.title}(${manga.id})" }
        }
        .singleOrNull()

    fun asFlow(mangaId: Long) = serverListeners.combineMangaUpdates(
        mangaRepository.getMangaFull(mangaId)
    ) { mangaId in it }

    fun asFlow(manga: Manga) = serverListeners.combineMangaUpdates(
        mangaRepository.getMangaFull(manga.id)
    ) { manga.id in it }

    companion object {
        private val log = logging()
    }
}
