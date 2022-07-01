/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.domain.chapter.interactor

import ca.gosyer.jui.domain.chapter.service.ChapterRepository
import ca.gosyer.jui.domain.manga.model.Manga
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.singleOrNull
import me.tatarka.inject.annotations.Inject
import org.lighthousegames.logging.logging

class RefreshChapters @Inject constructor(private val chapterRepository: ChapterRepository) {

    suspend fun await(mangaId: Long) = asFlow(mangaId)
        .catch { log.warn(it) { "Failed to refresh chapters for $mangaId" } }
        .singleOrNull()

    suspend fun await(manga: Manga) = asFlow(manga)
        .catch { log.warn(it) { "Failed to refresh chapters for ${manga.title}(${manga.id})" } }
        .singleOrNull()

    fun asFlow(mangaId: Long) = chapterRepository.getChapters(mangaId, true)

    fun asFlow(manga: Manga) = chapterRepository.getChapters(manga.id, true)

    companion object {
        private val log = logging()
    }
}
