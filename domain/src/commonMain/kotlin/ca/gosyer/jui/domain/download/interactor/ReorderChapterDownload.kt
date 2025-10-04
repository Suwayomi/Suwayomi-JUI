/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.domain.download.interactor

import ca.gosyer.jui.domain.download.service.DownloadRepository
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import me.tatarka.inject.annotations.Inject
import org.lighthousegames.logging.logging

@Inject
class ReorderChapterDownload(
    private val downloadRepository: DownloadRepository,
) {
    suspend fun await(
        chapterId: Long,
        to: Int,
        onError: suspend (Throwable) -> Unit = {},
    ) = asFlow(chapterId, to)
        .catch {
            onError(it)
            log.warn(it) { "Failed to reorder chapter download for $chapterId to $to" }
        }
        .collect()

    fun asFlow(
        chapterId: Long,
        to: Int,
    ) = downloadRepository.reorderChapterDownload(chapterId, to)

    companion object {
        private val log = logging()
    }
}
