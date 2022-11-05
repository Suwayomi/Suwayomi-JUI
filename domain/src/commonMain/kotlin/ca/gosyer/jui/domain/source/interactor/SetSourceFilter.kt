/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.domain.source.interactor

import ca.gosyer.jui.domain.source.model.Source
import ca.gosyer.jui.domain.source.model.sourcefilters.SourceFilterChange
import ca.gosyer.jui.domain.source.service.SourceRepository
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import me.tatarka.inject.annotations.Inject
import org.lighthousegames.logging.logging

class SetSourceFilter @Inject constructor(private val sourceRepository: SourceRepository) {

    suspend fun await(source: Source, filterIndex: Int, filter: Any) = asFlow(source, filterIndex, filter)
        .catch { log.warn(it) { "Failed to set filter for ${source.displayName} with index = $filterIndex and value = $filter" } }
        .collect()

    suspend fun await(sourceId: Long, filterIndex: Int, filter: Any) = asFlow(sourceId, filterIndex, filter)
        .catch { log.warn(it) { "Failed to set filter for $sourceId with index = $filterIndex and value = $filter" } }
        .collect()

    suspend fun await(source: Source, filterIndex: Int, childFilterIndex: Int, filter: Any) = asFlow(source, filterIndex, childFilterIndex, filter)
        .catch { log.warn(it) { "Failed to set filter for ${source.displayName} with index = $filterIndex and childIndex = $childFilterIndex and value = $filter" } }
        .collect()

    suspend fun await(sourceId: Long, filterIndex: Int, childFilterIndex: Int, filter: Any) = asFlow(sourceId, filterIndex, childFilterIndex, filter)
        .catch { log.warn(it) { "Failed to set filter for $sourceId with index = $filterIndex and childIndex = $childFilterIndex and value = $filter" } }
        .collect()

    fun asFlow(source: Source, filterIndex: Int, filter: Any) = sourceRepository.setFilter(
        source.id,
        SourceFilterChange(filterIndex, filter)
    )

    fun asFlow(sourceId: Long, filterIndex: Int, filter: Any) = sourceRepository.setFilter(
        sourceId,
        SourceFilterChange(filterIndex, filter)
    )

    fun asFlow(source: Source, filterIndex: Int, childFilterIndex: Int, filter: Any) = sourceRepository.setFilter(
        source.id,
        SourceFilterChange(filterIndex, Json.encodeToString(SourceFilterChange(childFilterIndex, filter)))
    )

    fun asFlow(sourceId: Long, filterIndex: Int, childFilterIndex: Int,filter: Any) = sourceRepository.setFilter(
        sourceId,
        SourceFilterChange(filterIndex, Json.encodeToString(SourceFilterChange(childFilterIndex, filter)))
    )

    companion object {
        private val log = logging()
    }
}