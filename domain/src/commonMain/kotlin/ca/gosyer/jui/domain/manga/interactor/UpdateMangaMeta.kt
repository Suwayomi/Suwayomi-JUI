/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.domain.manga.interactor

import ca.gosyer.jui.domain.manga.model.Manga
import ca.gosyer.jui.domain.manga.service.MangaRepository
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import me.tatarka.inject.annotations.Inject

class UpdateMangaMeta @Inject constructor(private val mangaRepository: MangaRepository) {

    fun subscribe(
        manga: Manga,
        readerMode: String = manga.meta.juiReaderMode
    ) = flow {
        if (readerMode != manga.meta.juiReaderMode) {
            emitAll(
                mangaRepository.updateMangaMeta(
                    manga.id,
                    "juiReaderMode",
                    readerMode
                )
            )
        }
    }
}
