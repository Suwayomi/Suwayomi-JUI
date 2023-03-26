/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.domain.manga.service

import ca.gosyer.jui.domain.manga.model.Manga
import de.jensklingenberg.ktorfit.http.Field
import de.jensklingenberg.ktorfit.http.FormUrlEncoded
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.PATCH
import de.jensklingenberg.ktorfit.http.Path
import de.jensklingenberg.ktorfit.http.Query
import de.jensklingenberg.ktorfit.http.ReqBuilder
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.statement.HttpResponse
import io.ktor.utils.io.ByteReadChannel
import kotlinx.coroutines.flow.Flow

interface MangaRepository {
    @GET("api/v1/manga/{mangaId}/")
    fun getManga(
        @Path("mangaId") mangaId: Long,
        @Query("onlineFetch") refresh: Boolean = false,
    ): Flow<Manga>

    @GET("api/v1/manga/{mangaId}/full")
    fun getMangaFull(
        @Path("mangaId") mangaId: Long,
        @Query("onlineFetch") refresh: Boolean = false,
    ): Flow<Manga>

    @GET("api/v1/manga/{mangaId}/thumbnail")
    fun getMangaThumbnail(
        @Path("mangaId") mangaId: Long,
        @ReqBuilder block: HttpRequestBuilder.() -> Unit,
    ): Flow<ByteReadChannel>

    @PATCH("api/v1/manga/{mangaId}/meta")
    @FormUrlEncoded
    fun updateMangaMeta(
        @Path("mangaId") mangaId: Long,
        @Field("key") key: String,
        @Field("value") value: String,
    ): Flow<HttpResponse>
}
