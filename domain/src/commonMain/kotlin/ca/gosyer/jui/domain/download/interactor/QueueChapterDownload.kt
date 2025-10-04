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

class QueueChapterDownload
    @Inject
    constructor(
        private val downloadRepository: DownloadRepository,
    ) {
        suspend fun await(
            chapterId: Long,
            onError: suspend (Throwable) -> Unit = {},
        ) = asFlow(chapterId)
            .catch {
                onError(it)
                log.warn(it) { "Failed to queue chapter $chapterId for a download" }
            }
            .collect()

        fun asFlow(chapterId: Long) = downloadRepository.queueChapterDownload(chapterId)

        companion object {
            private val log = logging()
        }
    }
