/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.domain.source.service

import ca.gosyer.jui.domain.source.model.MangaPage
import ca.gosyer.jui.domain.source.model.Source
import ca.gosyer.jui.domain.source.model.sourcefilters.SourceFilter
import ca.gosyer.jui.domain.source.model.sourcepreference.SourcePreference
import kotlinx.coroutines.flow.Flow

interface SourceRepository {
    fun getSourceList(): Flow<List<Source>>

    fun getSourceInfo(sourceId: Long): Flow<Source>

    fun getPopularManga(
        sourceId: Long,
        pageNum: Int,
    ): Flow<MangaPage>

    fun getLatestManga(
        sourceId: Long,
        pageNum: Int,
    ): Flow<MangaPage>

    fun getFilterList(sourceId: Long): Flow<List<SourceFilter>>

    fun getSearchResults(
        sourceId: Long,
        pageNum: Int,
        searchTerm: String?,
        filters: List<SourceFilter>?,
    ): Flow<MangaPage>

    fun getSourceSettings(sourceId: Long): Flow<List<SourcePreference>>

    fun setSourceSetting(
        sourceId: Long,
        sourcePreference: SourcePreference,
    ): Flow<Unit>
}
