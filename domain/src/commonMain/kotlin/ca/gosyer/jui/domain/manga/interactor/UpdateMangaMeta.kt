/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.domain.manga.interactor

import ca.gosyer.jui.domain.manga.model.Manga
import ca.gosyer.jui.domain.manga.service.MangaRepository
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import me.tatarka.inject.annotations.Inject
import org.lighthousegames.logging.logging

class UpdateMangaMeta @Inject constructor(private val mangaRepository: MangaRepository) {

    suspend fun await(
        manga: Manga,
        readerMode: String = manga.meta.juiReaderMode,
        onError: suspend (Throwable) -> Unit = {}
    ) = asFlow(manga, readerMode)
        .catch {
            onError(it)
            log.warn(it) { "Failed to update ${manga.title}(${manga.id}) meta" }
        }
        .collect()

    fun asFlow(
        manga: Manga,
        readerMode: String = manga.meta.juiReaderMode
    ) = flow {
        if (readerMode != manga.meta.juiReaderMode) {
            mangaRepository.updateMangaMeta(
                manga.id,
                "juiReaderMode",
                readerMode
            ).collect()
        }
        emit(Unit)
    }

    companion object {
        private val log = logging()
    }
}
