/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.domain.source.interactor

import ca.gosyer.jui.domain.source.model.Source
import ca.gosyer.jui.domain.source.model.sourcefilters.SourceFilter
import ca.gosyer.jui.domain.source.service.SourceRepository
import com.diamondedge.logging.logging
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.singleOrNull
import me.tatarka.inject.annotations.Inject

@Inject
class GetSearchManga(
    private val sourceRepository: SourceRepository,
) {
    suspend fun await(
        source: Source,
        page: Int,
        searchTerm: String?,
        filters: List<SourceFilter>?,
        onError: suspend (Throwable) -> Unit = {
        },
    ) = asFlow(source.id, page, searchTerm, filters)
        .catch {
            onError(it)
            log.warn(it) { "Failed to get search results from ${source.displayName} on page $page with query '$searchTerm'" }
        }
        .singleOrNull()

    suspend fun await(
        sourceId: Long,
        searchTerm: String?,
        page: Int,
        filters: List<SourceFilter>?,
        onError: suspend (Throwable) -> Unit = {
        },
    ) = asFlow(sourceId, page, searchTerm, filters)
        .catch {
            onError(it)
            log.warn(it) { "Failed to get search results from $sourceId on page $page with query '$searchTerm'" }
        }
        .singleOrNull()

    fun asFlow(
        source: Source,
        page: Int,
        searchTerm: String?,
        filters: List<SourceFilter>?,
    ) = sourceRepository.getSearchResults(
        source.id,
        page,
        searchTerm?.ifBlank { null },
        filters,
    )

    fun asFlow(
        sourceId: Long,
        page: Int,
        searchTerm: String?,
        filters: List<SourceFilter>?,
    ) = sourceRepository.getSearchResults(
        sourceId,
        page,
        searchTerm?.ifBlank { null },
        filters,
    )

    companion object {
        private val log = logging()
    }
}
