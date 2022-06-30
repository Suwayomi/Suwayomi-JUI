/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.domain.chapter.interactor

import ca.gosyer.jui.domain.chapter.model.Chapter
import ca.gosyer.jui.domain.chapter.service.ChapterRepository
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import me.tatarka.inject.annotations.Inject

class UpdateChapterMeta @Inject constructor(private val chapterRepository: ChapterRepository) {

    fun subscribe(
        chapter: Chapter,
        pageOffset: Int = chapter.meta.juiPageOffset
    ) = flow {
        if (pageOffset != chapter.meta.juiPageOffset) {
            emitAll(
                chapterRepository.updateChapterMeta(
                    chapter.mangaId,
                    chapter.index,
                    "juiPageOffset",
                    pageOffset.toString()
                )
            )
        }
    }
}
