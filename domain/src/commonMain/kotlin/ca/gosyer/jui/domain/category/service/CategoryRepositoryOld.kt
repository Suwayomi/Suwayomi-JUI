/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.domain.category.service

import ca.gosyer.jui.domain.category.model.Category
import ca.gosyer.jui.domain.manga.model.Manga
import de.jensklingenberg.ktorfit.http.DELETE
import de.jensklingenberg.ktorfit.http.Field
import de.jensklingenberg.ktorfit.http.FormUrlEncoded
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.PATCH
import de.jensklingenberg.ktorfit.http.POST
import de.jensklingenberg.ktorfit.http.Path
import io.ktor.client.statement.HttpResponse
import kotlinx.coroutines.flow.Flow

interface CategoryRepositoryOld {
    @GET("api/v1/manga/{mangaId}/category/")
    fun getMangaCategories(
        @Path("mangaId") mangaId: Long,
    ): Flow<List<Category>>

    @GET("api/v1/manga/{mangaId}/category/{categoryId}")
    fun addMangaToCategory(
        @Path("mangaId") mangaId: Long,
        @Path("categoryId") categoryId: Long,
    ): Flow<HttpResponse>

    @DELETE("api/v1/manga/{mangaId}/category/{categoryId}")
    fun removeMangaFromCategory(
        @Path("mangaId") mangaId: Long,
        @Path("categoryId") categoryId: Long,
    ): Flow<HttpResponse>

    @GET("api/v1/category/")
    fun getCategories(): Flow<List<Category>>

    @FormUrlEncoded
    @POST("api/v1/category/")
    fun createCategory(
        @Field("name") name: String,
    ): Flow<HttpResponse>

    @FormUrlEncoded
    @PATCH("api/v1/category/{categoryId}")
    fun modifyCategory(
        @Path("categoryId") categoryId: Long,
        @Field("name") name: String,
    ): Flow<HttpResponse>

    @FormUrlEncoded
    @PATCH("api/v1/category/reorder")
    fun reorderCategory(
        @Field("to") to: Int,
        @Field("from") from: Int,
    ): Flow<HttpResponse>

    @DELETE("api/v1/category/{categoryId}")
    fun deleteCategory(
        @Path("categoryId") categoryId: Long,
    ): Flow<HttpResponse>

    @GET("api/v1/category/{categoryId}")
    fun getMangaFromCategory(
        @Path("categoryId") categoryId: Long,
    ): Flow<List<Manga>>

    @FormUrlEncoded
    @PATCH("api/v1/category/{categoryId}/meta")
    fun updateCategoryMeta(
        @Path("categoryId") categoryId: Long,
        @Field("key") key: String,
        @Field("value") value: String,
    ): Flow<HttpResponse>
}
