/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.domain.download.service

import ca.gosyer.jui.domain.download.model.DownloadEnqueue
import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.DELETE
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Headers
import de.jensklingenberg.ktorfit.http.PATCH
import de.jensklingenberg.ktorfit.http.POST
import de.jensklingenberg.ktorfit.http.Path
import io.ktor.client.statement.HttpResponse
import kotlinx.coroutines.flow.Flow

interface DownloadRepository {
    @GET("api/v1/downloads/start")
    fun startDownloading(): Flow<HttpResponse>

    @GET("api/v1/downloads/stop")
    fun stopDownloading(): Flow<HttpResponse>

    @GET("api/v1/downloads/clear")
    fun clearDownloadQueue(): Flow<HttpResponse>

    @GET("api/v1/download/{mangaId}/chapter/{chapterIndex}")
    fun queueChapterDownload(
        @Path("mangaId") mangaId: Long,
        @Path("chapterIndex") chapterIndex: Int
    ): Flow<HttpResponse>

    @DELETE("api/v1/download/{mangaId}/chapter/{chapterIndex}")
    fun stopChapterDownload(
        @Path("mangaId") mangaId: Long,
        @Path("chapterIndex") chapterIndex: Int
    ): Flow<HttpResponse>

    @PATCH("api/v1/download/{mangaId}/chapter/{chapterIndex}/reorder/{to}")
    fun reorderChapterDownload(
        @Path("mangaId") mangaId: Long,
        @Path("chapterIndex") chapterIndex: Int,
        @Path("to") to: Int
    ): Flow<HttpResponse>

    @POST("api/v1/download/batch")
    @Headers("Content-Type: application/json")
    fun batchDownload(
        @Body downloadEnqueue: DownloadEnqueue
    ): Flow<HttpResponse>
}
