/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.domain.source.interactor

import ca.gosyer.jui.domain.source.service.SourceRepositoryOld
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.singleOrNull
import me.tatarka.inject.annotations.Inject
import org.lighthousegames.logging.logging

class GetSourceList
    @Inject
    constructor(
        private val sourceRepositoryOld: SourceRepositoryOld,
    ) {
        suspend fun await(onError: suspend (Throwable) -> Unit = {}) =
            asFlow()
                .catch {
                    onError(it)
                    log.warn(it) { "Failed to get source list" }
                }
                .singleOrNull()

        fun asFlow() = sourceRepositoryOld.getSourceList()

        companion object {
            private val log = logging()
        }
    }
