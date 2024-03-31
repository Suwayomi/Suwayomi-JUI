/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.domain.updates.interactor

import ca.gosyer.jui.domain.updates.service.UpdatesRepositoryOld
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.singleOrNull
import me.tatarka.inject.annotations.Inject
import org.lighthousegames.logging.logging

class GetRecentUpdates
    @Inject
    constructor(
        private val updatesRepositoryOld: UpdatesRepositoryOld,
    ) {
        suspend fun await(
            pageNum: Int,
            onError: suspend (Throwable) -> Unit = {},
        ) = asFlow(pageNum)
            .catch {
                onError(it)
                log.warn(it) { "Failed to get updates for page $pageNum" }
            }
            .singleOrNull()

        fun asFlow(pageNum: Int) = updatesRepositoryOld.getRecentUpdates(pageNum)

        companion object {
            private val log = logging()
        }
    }
