/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.domain.source.interactor

import ca.gosyer.jui.domain.source.model.sourcepreference.SourcePreference
import ca.gosyer.jui.domain.source.service.SourceRepository
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import me.tatarka.inject.annotations.Inject
import org.lighthousegames.logging.logging

class SetSourceSetting
    @Inject
    constructor(
        private val sourceRepository: SourceRepository,
    ) {
        suspend fun await(
            sourceId: Long,
            sourcePreference: SourcePreference,
            onError: suspend (Throwable) -> Unit = {
            },
        ) = asFlow(sourceId, sourcePreference)
            .catch {
                onError(it)
                log.warn(it) { "Failed to set setting for $sourceId with index = ${sourcePreference.position}" }
            }
            .collect()

        fun asFlow(
            sourceId: Long,
            sourcePreference: SourcePreference,
        ) = sourceRepository.setSourceSetting(
            sourceId,
            sourcePreference,
        )

        companion object {
            private val log = logging()
        }
    }
