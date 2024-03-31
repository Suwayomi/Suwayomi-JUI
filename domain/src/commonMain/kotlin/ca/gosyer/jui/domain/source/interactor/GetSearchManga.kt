/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.domain.source.interactor

import ca.gosyer.jui.domain.source.model.Source
import ca.gosyer.jui.domain.source.service.SourceRepositoryOld
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.singleOrNull
import me.tatarka.inject.annotations.Inject
import org.lighthousegames.logging.logging

class GetSearchManga
    @Inject
    constructor(
        private val sourceRepositoryOld: SourceRepositoryOld,
    ) {
        suspend fun await(
            source: Source,
            searchTerm: String?,
            page: Int,
            onError: suspend (Throwable) -> Unit = {
            },
        ) = asFlow(source.id, searchTerm, page)
            .catch {
                onError(it)
                log.warn(it) { "Failed to get search results from ${source.displayName} on page $page with query '$searchTerm'" }
            }
            .singleOrNull()

        suspend fun await(
            sourceId: Long,
            searchTerm: String?,
            page: Int,
            onError: suspend (Throwable) -> Unit = {
            },
        ) = asFlow(sourceId, searchTerm, page)
            .catch {
                onError(it)
                log.warn(it) { "Failed to get search results from $sourceId on page $page with query '$searchTerm'" }
            }
            .singleOrNull()

        fun asFlow(
            source: Source,
            searchTerm: String?,
            page: Int,
        ) = sourceRepositoryOld.getSearchResults(
            source.id,
            searchTerm?.ifBlank { null },
            page,
        )

        fun asFlow(
            sourceId: Long,
            searchTerm: String?,
            page: Int,
        ) = sourceRepositoryOld.getSearchResults(
            sourceId,
            searchTerm?.ifBlank { null },
            page,
        )

        companion object {
            private val log = logging()
        }
    }
