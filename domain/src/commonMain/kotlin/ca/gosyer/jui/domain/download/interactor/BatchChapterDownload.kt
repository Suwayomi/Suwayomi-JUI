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

class BatchChapterDownload
    @Inject
    constructor(
        private val downloadRepository: DownloadRepository,
    ) {
        suspend fun await(
            chapterIds: List<Long>,
            onError: suspend (Throwable) -> Unit = {},
        ) = asFlow(chapterIds)
            .catch {
                onError(it)
                log.warn(it) { "Failed to queue chapters $chapterIds for a download" }
            }
            .collect()

        suspend fun await(
            vararg chapterIds: Long,
            onError: suspend (Throwable) -> Unit = {},
        ) = asFlow(*chapterIds)
            .catch {
                onError(it)
                log.warn(it) { "Failed to queue chapters ${chapterIds.asList()} for a download" }
            }
            .collect()

        fun asFlow(chapterIds: List<Long>) = downloadRepository.batchDownload(chapterIds)

        fun asFlow(vararg chapterIds: Long) = downloadRepository.batchDownload(chapterIds.asList())

        companion object {
            private val log = logging()
        }
    }
