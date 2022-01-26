/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.data.server.requests

@Get
fun getMangaCategoriesQuery(mangaId: Long) =
    "/api/v1/manga/$mangaId/category/"

@Get
fun addMangaToCategoryQuery(mangaId: Long, categoryId: Long) =
    "/api/v1/manga/$mangaId/category/$categoryId"

@Delete
fun removeMangaFromCategoryRequest(mangaId: Long, categoryId: Long) =
    "/api/v1/manga/$mangaId/category/$categoryId"

@Get
fun getCategoriesQuery() =
    "/api/v1/category/"

/**
 * Post a formbody with the param {name} for creation of a category
 */
@Post
fun createCategoryRequest() =
    "/api/v1/category/"

@Patch
fun categoryModifyRequest(categoryId: Long) =
    "/api/v1/category/$categoryId"

@Patch
fun categoryReorderRequest() =
    "/api/v1/category/reorder"

@Delete
fun categoryDeleteRequest(categoryId: Long) =
    "/api/v1/category/$categoryId"

@Get
fun getMangaInCategoryQuery(categoryId: Long) =
    "/api/v1/category/$categoryId"
