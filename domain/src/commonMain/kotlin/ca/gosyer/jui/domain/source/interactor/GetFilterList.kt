/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.domain.source.interactor

import ca.gosyer.jui.domain.source.model.Source
import ca.gosyer.jui.domain.source.service.SourceRepository
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.singleOrNull
import me.tatarka.inject.annotations.Inject
import org.lighthousegames.logging.logging

class GetFilterList
    @Inject
    constructor(private val sourceRepository: SourceRepository) {
        suspend fun await(
            source: Source,
            reset: Boolean,
            onError: suspend (Throwable) -> Unit = {},
        ) = asFlow(source.id, reset)
            .catch {
                onError(it)
                log.warn(it) { "Failed to get filter list for ${source.displayName} with reset = $reset" }
            }
            .singleOrNull()

        suspend fun await(
            sourceId: Long,
            reset: Boolean,
            onError: suspend (Throwable) -> Unit = {},
        ) = asFlow(sourceId, reset)
            .catch {
                onError(it)
                log.warn(it) { "Failed to get filter list for $sourceId with reset = $reset" }
            }
            .singleOrNull()

        fun asFlow(
            source: Source,
            reset: Boolean,
        ) = sourceRepository.getFilterList(source.id, reset)

        fun asFlow(
            sourceId: Long,
            reset: Boolean,
        ) = sourceRepository.getFilterList(sourceId, reset)

        companion object {
            private val log = logging()
        }
    }
