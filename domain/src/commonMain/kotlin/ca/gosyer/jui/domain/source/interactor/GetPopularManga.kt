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

class GetPopularManga @Inject constructor(private val sourceRepository: SourceRepository) {

    suspend fun await(source: Source, page: Int, onError: suspend (Throwable) -> Unit = {}) = asFlow(source.id, page)
        .catch {
            onError(it)
            log.warn(it) { "Failed to get popular manga from ${source.displayName} on page $page" }
        }
        .singleOrNull()

    suspend fun await(sourceId: Long, page: Int, onError: suspend (Throwable) -> Unit = {}) = asFlow(sourceId, page)
        .catch {
            onError(it)
            log.warn(it) { "Failed to get popular manga from $sourceId on page $page" }
        }
        .singleOrNull()

    fun asFlow(source: Source, page: Int) = sourceRepository.getPopularManga(source.id, page)

    fun asFlow(sourceId: Long, page: Int) = sourceRepository.getPopularManga(sourceId, page)

    companion object {
        private val log = logging()
    }
}
