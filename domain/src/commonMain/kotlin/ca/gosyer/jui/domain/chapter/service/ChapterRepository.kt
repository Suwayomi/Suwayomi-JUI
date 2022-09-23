/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.domain.chapter.service

import ca.gosyer.jui.domain.chapter.model.Chapter
import de.jensklingenberg.ktorfit.http.DELETE
import de.jensklingenberg.ktorfit.http.Field
import de.jensklingenberg.ktorfit.http.FormUrlEncoded
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.PATCH
import de.jensklingenberg.ktorfit.http.Path
import de.jensklingenberg.ktorfit.http.Query
import de.jensklingenberg.ktorfit.http.ReqBuilder
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.statement.HttpResponse
import kotlinx.coroutines.flow.Flow

interface ChapterRepository {

    @GET("api/v1/manga/{mangaId}/chapters")
    fun getChapters(
        @Path("mangaId") mangaId: Long,
        @Query("onlineFetch") refresh: Boolean = false
    ): Flow<List<Chapter>>

    @GET("api/v1/manga/{mangaId}/chapter/{chapterIndex}")
    fun getChapter(
        @Path("mangaId") mangaId: Long,
        @Path("chapterIndex") chapterIndex: Int
    ): Flow<Chapter>

    /* TODO add once ktorfit supports nullable paremters
    @FormUrlEncoded
    @PATCH("/api/v1/manga/{mangaId}/chapter/{chapterIndex}")
    fun updateChapter(
        @Path("mangaId") mangaId: Long,
        @Path("chapterIndex") chapterIndex: Int,
        @Field("read") read: Boolean? = null,
        @Field("bookmarked") bookmarked: Boolean? = null,
        @Field("lastPageRead") lastPageRead: Int? = null,
        @Field("markPrevRead") markPreviousRead: Boolean? = null
    ): Flow<HttpResponse>*/

    //todo remove following updateChapter functions once ktorfit supports nullable parameters
    @FormUrlEncoded
    @PATCH("api/v1/manga/{mangaId}/chapter/{chapterIndex}")
    fun updateChapterRead(
        @Path("mangaId") mangaId: Long,
        @Path("chapterIndex") chapterIndex: Int,
        @Field("read") read: Boolean,
    ): Flow<HttpResponse>

    @FormUrlEncoded
    @PATCH("api/v1/manga/{mangaId}/chapter/{chapterIndex}")
    fun updateChapterBookmarked(
        @Path("mangaId") mangaId: Long,
        @Path("chapterIndex") chapterIndex: Int,
        @Field("bookmarked") bookmarked: Boolean,
    ): Flow<HttpResponse>

    @FormUrlEncoded
    @PATCH("api/v1/manga/{mangaId}/chapter/{chapterIndex}")
    fun updateChapterLastPageRead(
        @Path("mangaId") mangaId: Long,
        @Path("chapterIndex") chapterIndex: Int,
        @Field("lastPageRead") lastPageRead: Int,
    ): Flow<HttpResponse>

    @FormUrlEncoded
    @PATCH("api/v1/manga/{mangaId}/chapter/{chapterIndex}")
    fun updateChapterMarkPrevRead(
        @Path("mangaId") mangaId: Long,
        @Path("chapterIndex") chapterIndex: Int,
        @Field("markPrevRead") markPreviousRead: Boolean = true
    ): Flow<HttpResponse>

    @GET("api/v1/manga/{mangaId}/chapter/{chapterIndex}/page/{pageNum}")
    fun getPage(
        @Path("mangaId") mangaId: Long,
        @Path("chapterIndex") chapterIndex: Int,
        @Path("pageNum") pageNum: Int,
        @ReqBuilder block: HttpRequestBuilder.() -> Unit
    ): Flow<HttpResponse>

    @DELETE("api/v1/manga/{mangaId}/chapter/{chapterIndex}")
    fun deleteChapterDownload(
        @Path("mangaId") mangaId: Long,
        @Path("chapterIndex") chapterIndex: Int
    ): Flow<HttpResponse>

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

    @FormUrlEncoded
    @PATCH("api/v1/manga/{mangaId}/chapter/{chapterIndex}/meta")
    fun updateChapterMeta(
        @Path("mangaId") mangaId: Long,
        @Path("chapterIndex") chapterIndex: Int,
        @Field("key") key: String,
        @Field("value") value: String
    ): Flow<HttpResponse>
}
