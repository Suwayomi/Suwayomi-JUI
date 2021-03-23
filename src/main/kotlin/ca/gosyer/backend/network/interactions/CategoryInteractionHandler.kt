/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.backend.network.interactions

import ca.gosyer.backend.models.Category
import ca.gosyer.backend.models.Manga
import ca.gosyer.backend.network.requests.addMangaToCategoryQuery
import ca.gosyer.backend.network.requests.categoryDeleteRequest
import ca.gosyer.backend.network.requests.categoryModifyRequest
import ca.gosyer.backend.network.requests.categoryReorderRequest
import ca.gosyer.backend.network.requests.createCategoryRequest
import ca.gosyer.backend.network.requests.getCategoriesQuery
import ca.gosyer.backend.network.requests.getMangaCategoriesQuery
import ca.gosyer.backend.network.requests.getMangaInCategoryQuery
import ca.gosyer.backend.network.requests.removeMangaFromCategoryRequest
import io.ktor.client.HttpClient
import io.ktor.client.request.forms.formData
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpMethod
import io.ktor.http.Parameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CategoryInteractionHandler(private val client: HttpClient): BaseInteractionHandler() {

    suspend fun getMangaCategories(mangaId: Long) = withContext(Dispatchers.IO) {
        client.getRepeat<List<Category>>(
            serverUrl + getMangaCategoriesQuery(mangaId)
        )
    }

    suspend fun addMangaToCategory(mangaId: Long, categoryId: Long) = withContext(Dispatchers.IO) {
        client.getRepeat<HttpResponse>(
            serverUrl + addMangaToCategoryQuery(mangaId, categoryId)
        )
    }

    suspend fun removeMangaFromCategory(mangaId: Long, categoryId: Long) = withContext(Dispatchers.IO) {
        client.deleteRepeat<HttpResponse>(
            serverUrl + removeMangaFromCategoryRequest(mangaId, categoryId)
        )
    }

    suspend fun getCategories() = withContext(Dispatchers.IO) {
        client.getRepeat<List<Category>>(
            serverUrl + getCategoriesQuery()
        )
    }

    suspend fun createCategory(name: String) = withContext(Dispatchers.IO) {
        client.submitFormRepeat<HttpResponse>(
            serverUrl + createCategoryRequest(),
            formParameters = Parameters.build {
                append("name", name)
            }
        )
    }

    suspend fun modifyCategory(categoryId: Long, name: String? = null, isLanding: Boolean? = null) = withContext(Dispatchers.IO) {
        client.submitFormRepeat<HttpResponse>(
            serverUrl + categoryModifyRequest(categoryId),
                formParameters = Parameters.build {
                    if (name != null) {
                        append("name", name)
                    }
                    if (isLanding != null) {
                        append("isLanding", isLanding.toString())
                    }
            }
        ) {
            method = HttpMethod.Patch
            formData {
                if (name != null) {
                    append("name", name)
                }
                if (isLanding != null) {
                    append("isLanding", isLanding.toString())
                }
            }
        }
    }

    suspend fun reorderCategory(categoryId: Long, to: Int, from: Int) = withContext(Dispatchers.IO) {
        client.submitFormRepeat<HttpResponse>(
            serverUrl + categoryReorderRequest(categoryId),
            formParameters = Parameters.build {
                append("to", to.toString())
                append("from", from.toString())
            }
        ) {
            method = HttpMethod.Patch
        }
    }

    suspend fun deleteCategory(categoryId: Long) = withContext(Dispatchers.IO) {
        client.deleteRepeat<HttpResponse>(
            serverUrl + categoryDeleteRequest(categoryId)
        )
    }

    suspend fun getMangaFromCategory(categoryId: Long) = withContext(Dispatchers.IO) {
        client.getRepeat<List<Manga>>(
            serverUrl + getMangaInCategoryQuery(categoryId)
        )
    }
}