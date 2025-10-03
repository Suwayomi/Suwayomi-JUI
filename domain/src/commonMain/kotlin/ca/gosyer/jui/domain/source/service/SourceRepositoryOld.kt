/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.domain.source.service

import ca.gosyer.jui.domain.source.model.MangaPage
import ca.gosyer.jui.domain.source.model.Source
import ca.gosyer.jui.domain.source.model.sourcefilters.SourceFilterChangeOld
import ca.gosyer.jui.domain.source.model.sourcefilters.SourceFilterData
import ca.gosyer.jui.domain.source.model.sourcefilters.SourceFilterOld
import ca.gosyer.jui.domain.source.model.sourcepreference.SourcePreferenceChange
import ca.gosyer.jui.domain.source.model.sourcepreference.SourcePreferenceOld
import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Headers
import de.jensklingenberg.ktorfit.http.POST
import de.jensklingenberg.ktorfit.http.Path
import de.jensklingenberg.ktorfit.http.Query
import io.ktor.client.statement.HttpResponse
import kotlinx.coroutines.flow.Flow

interface SourceRepositoryOld {
    @GET("api/v1/source/list")
    fun getSourceList(): Flow<List<Source>>

    @GET("api/v1/source/{sourceId}")
    fun getSourceInfo(
        @Path("sourceId") sourceId: Long,
    ): Flow<Source>

    @GET("api/v1/source/{sourceId}/popular/{pageNum}")
    fun getPopularManga(
        @Path("sourceId") sourceId: Long,
        @Path("pageNum") pageNum: Int,
    ): Flow<MangaPage>

    @GET("api/v1/source/{sourceId}/latest/{pageNum}")
    fun getLatestManga(
        @Path("sourceId") sourceId: Long,
        @Path("pageNum") pageNum: Int,
    ): Flow<MangaPage>

    @GET("api/v1/source/{sourceId}/search")
    fun getSearchResults(
        @Path("sourceId") sourceId: Long,
        @Query("searchTerm") searchTerm: String?,
        @Query("pageNum") pageNum: Int,
    ): Flow<MangaPage>

    @GET("api/v1/source/{sourceId}/filters")
    fun getFilterList(
        @Path("sourceId") sourceId: Long,
        @Query("reset") reset: Boolean = false,
    ): Flow<List<SourceFilterOld>>

    @POST("api/v1/source/{sourceId}/filters")
    @Headers("Content-Type: application/json")
    fun setFilter(
        @Path("sourceId") sourceId: Long,
        @Body sourceFilter: SourceFilterChangeOld,
    ): Flow<HttpResponse>

    @POST("api/v1/source/{sourceId}/filters")
    @Headers("Content-Type: application/json")
    fun setFilters(
        @Path("sourceId") sourceId: Long,
        @Body sourceFilters: List<SourceFilterChangeOld>,
    ): Flow<HttpResponse>

    @POST("api/v1/source/{sourceId}/quick-search")
    @Headers("Content-Type: application/json")
    fun getQuickSearchResults(
        @Path("sourceId") sourceId: Long,
        @Query("pageNum") pageNum: Int,
        @Body filterData: SourceFilterData,
    ): Flow<MangaPage>

    @GET("api/v1/source/{sourceId}/preferences")
    fun getSourceSettings(
        @Path("sourceId") sourceId: Long,
    ): Flow<List<SourcePreferenceOld>>

    @POST("api/v1/source/{sourceId}/preferences")
    @Headers("Content-Type: application/json")
    fun setSourceSetting(
        @Path("sourceId") sourceId: Long,
        @Body sourcePreference: SourcePreferenceChange,
    ): Flow<HttpResponse>
}
