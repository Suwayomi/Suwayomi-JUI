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

@Inject
class GetSourceSettings(
    private val sourceRepository: SourceRepository,
) {
    suspend fun await(
        source: Source,
        onError: suspend (Throwable) -> Unit = {},
    ) = asFlow(source.id)
        .catch {
            onError(it)
            log.warn(it) { "Failed to get source settings for ${source.displayName}" }
        }
        .singleOrNull()

    suspend fun await(
        sourceId: Long,
        onError: suspend (Throwable) -> Unit = {},
    ) = asFlow(sourceId)
        .catch {
            onError(it)
            log.warn(it) { "Failed to get source settings for $sourceId" }
        }
        .singleOrNull()

    fun asFlow(source: Source) = sourceRepository.getSourceSettings(source.id)

    fun asFlow(sourceId: Long) = sourceRepository.getSourceSettings(sourceId)

    companion object {
        private val log = logging()
    }
}
