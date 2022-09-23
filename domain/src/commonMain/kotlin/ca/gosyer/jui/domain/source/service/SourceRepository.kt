/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.domain.source.service

import ca.gosyer.jui.domain.source.model.MangaPage
import ca.gosyer.jui.domain.source.model.Source
import ca.gosyer.jui.domain.source.model.sourcefilters.SourceFilter
import ca.gosyer.jui.domain.source.model.sourcefilters.SourceFilterChange
import ca.gosyer.jui.domain.source.model.sourcepreference.SourcePreference
import ca.gosyer.jui.domain.source.model.sourcepreference.SourcePreferenceChange
import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.POST
import de.jensklingenberg.ktorfit.http.Path
import de.jensklingenberg.ktorfit.http.Query
import io.ktor.client.statement.HttpResponse
import kotlinx.coroutines.flow.Flow

interface SourceRepository {
    @GET("api/v1/source/list")
    fun getSourceList(): Flow<List<Source>>

    @GET("api/v1/source/{sourceId}")
    fun getSourceInfo(
        @Path("sourceId") sourceId: Long
    ): Flow<Source>

    @GET("api/v1/source/{sourceId}/popular/{pageNum}")
    fun getPopularManga(
        @Path("sourceId") sourceId: Long,
        @Path("pageNum") pageNum: Int
    ): Flow<MangaPage>

    @GET("api/v1/source/{sourceId}/latest/{pageNum}")
    fun getLatestManga(
        @Path("sourceId") sourceId: Long,
        @Path("pageNum") pageNum: Int
    ): Flow<MangaPage>

    @GET("api/v1/source/{sourceId}/search")
    fun getSearchResults(
        @Path("sourceId") sourceId: Long,
        @Query("searchTerm") searchTerm: String?,
        @Query("pageNum") pageNum: Int
    ): Flow<MangaPage>

    @GET("api/v1/source/{sourceId}/filters")
    fun getFilterList(
        @Path("sourceId") sourceId: Long,
        @Query("reset") reset: Boolean = false
    ): Flow<List<SourceFilter>>

    @POST("api/v1/source/{sourceId}/filters")
    fun setFilter(
        @Path("sourceId") sourceId: Long,
        @Body sourceFilter: SourceFilterChange
    ): Flow<HttpResponse>

    @GET("api/v1/source/{sourceId}/preferences")
    fun getSourceSettings(
        @Path("sourceId") sourceId: Long
    ): Flow<List<SourcePreference>>

    @POST("api/v1/source/{sourceId}/preferences")
    fun setSourceSetting(
        @Path("sourceId") sourceId: Long,
        @Body sourcePreference: SourcePreferenceChange
    ): Flow<HttpResponse>
}
