/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.domain.source.interactor

import ca.gosyer.jui.domain.source.model.Source
import ca.gosyer.jui.domain.source.model.sourcefilters.SourceFilterChange
import ca.gosyer.jui.domain.source.model.sourcefilters.SourceFilterData
import ca.gosyer.jui.domain.source.service.SourceRepository
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.singleOrNull
import me.tatarka.inject.annotations.Inject
import org.lighthousegames.logging.logging

class GetQuickSearchManga
    @Inject
    constructor(private val sourceRepository: SourceRepository) {
        suspend fun await(
            source: Source,
            searchTerm: String?,
            page: Int,
            filters: List<SourceFilterChange>?,
            onError: suspend (Throwable) -> Unit = {},
        ) = asFlow(source.id, searchTerm, page, filters)
            .catch {
                onError(it)
                log.warn(it) { "Failed to get quick search results from ${source.displayName} on page $page with query '$searchTerm'" }
            }
            .singleOrNull()

        suspend fun await(
            sourceId: Long,
            searchTerm: String?,
            page: Int,
            filters: List<SourceFilterChange>?,
            onError: suspend (Throwable) -> Unit = {},
        ) = asFlow(sourceId, searchTerm, page, filters)
            .catch {
                onError(it)
                log.warn(it) { "Failed to get quick search results from $sourceId on page $page with query '$searchTerm'" }
            }
            .singleOrNull()

        fun asFlow(
            source: Source,
            searchTerm: String?,
            page: Int,
            filters: List<SourceFilterChange>?,
        ) = sourceRepository.getQuickSearchResults(
            source.id,
            page,
            SourceFilterData(
                searchTerm?.ifBlank { null },
                filters?.ifEmpty { null },
            ),
        )

        fun asFlow(
            sourceId: Long,
            searchTerm: String?,
            page: Int,
            filters: List<SourceFilterChange>?,
        ) = sourceRepository.getQuickSearchResults(
            sourceId,
            page,
            SourceFilterData(
                searchTerm?.ifBlank { null },
                filters?.ifEmpty { null },
            ),
        )

        companion object {
            private val log = logging()
        }
    }
