/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.data.server.interactions

import ca.gosyer.jui.core.lang.IO
import ca.gosyer.jui.data.models.Category
import ca.gosyer.jui.data.models.Manga
import ca.gosyer.jui.data.server.Http
import ca.gosyer.jui.data.server.ServerPreferences
import ca.gosyer.jui.data.server.requests.addMangaToCategoryQuery
import ca.gosyer.jui.data.server.requests.categoryDeleteRequest
import ca.gosyer.jui.data.server.requests.categoryModifyRequest
import ca.gosyer.jui.data.server.requests.categoryReorderRequest
import ca.gosyer.jui.data.server.requests.createCategoryRequest
import ca.gosyer.jui.data.server.requests.getCategoriesQuery
import ca.gosyer.jui.data.server.requests.getMangaCategoriesQuery
import ca.gosyer.jui.data.server.requests.getMangaInCategoryQuery
import ca.gosyer.jui.data.server.requests.removeMangaFromCategoryRequest
import io.ktor.client.call.body
import io.ktor.client.plugins.expectSuccess
import io.ktor.client.request.delete
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.get
import io.ktor.http.HttpMethod
import io.ktor.http.Parameters
import io.ktor.http.path
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import me.tatarka.inject.annotations.Inject

class CategoryInteractionHandler @Inject constructor(
    client: Http,
    serverPreferences: ServerPreferences
) : BaseInteractionHandler(client, serverPreferences) {

    fun getMangaCategories(mangaId: Long) = flow {
        val response = client.get(
            buildUrl { path(getMangaCategoriesQuery(mangaId)) },
        ) {
            expectSuccess = true
        }.body<List<Category>>()
        emit(response)
    }.flowOn(Dispatchers.IO)

    fun getMangaCategories(manga: Manga) = getMangaCategories(manga.id)

    fun addMangaToCategory(mangaId: Long, categoryId: Long) = flow {
        val response = client.get(
            buildUrl { path(addMangaToCategoryQuery(mangaId, categoryId)) }
        ) {
            expectSuccess = true
        }
        emit(response)
    }.flowOn(Dispatchers.IO)
    fun addMangaToCategory(manga: Manga, category: Category) = addMangaToCategory(manga.id, category.id)
    fun addMangaToCategory(manga: Manga, categoryId: Long) = addMangaToCategory(manga.id, categoryId)
    fun addMangaToCategory(mangaId: Long, category: Category) = addMangaToCategory(mangaId, category.id)

    fun removeMangaFromCategory(mangaId: Long, categoryId: Long) = flow {
        val response = client.delete(
            buildUrl { path(removeMangaFromCategoryRequest(mangaId, categoryId)) }
        ) {
            expectSuccess = true
        }
        emit(response)
    }.flowOn(Dispatchers.IO)
    fun removeMangaFromCategory(manga: Manga, category: Category) = removeMangaFromCategory(manga.id, category.id)
    fun removeMangaFromCategory(manga: Manga, categoryId: Long) = removeMangaFromCategory(manga.id, categoryId)
    fun removeMangaFromCategory(mangaId: Long, category: Category) = removeMangaFromCategory(mangaId, category.id)

    fun getCategories(dropDefault: Boolean = false) = flow {
        val response = client.get(
            buildUrl { path(getCategoriesQuery()) },
        ) {
            expectSuccess = true
        }.body<List<Category>>().let { categories ->
            if (dropDefault) {
                categories.filterNot { it.name.equals("default", true) }
            } else categories
        }
        emit(response)
    }.flowOn(Dispatchers.IO)

    fun createCategory(name: String) = flow {
        val response = client.submitForm(
            buildUrl { path(createCategoryRequest()) },
            formParameters = Parameters.build {
                append("name", name)
            }
        ) {
            expectSuccess = true
        }
        emit(response)
    }.flowOn(Dispatchers.IO)

    fun modifyCategory(categoryId: Long, name: String? = null, isLanding: Boolean? = null) = flow {
        val response = client.submitForm(
            buildUrl { path(categoryModifyRequest(categoryId)) },
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
            expectSuccess = true
        }
        emit(response)
    }.flowOn(Dispatchers.IO)
    fun modifyCategory(category: Category, name: String? = null, isLanding: Boolean? = null) = modifyCategory(category.id, name, isLanding)

    fun reorderCategory(to: Int, from: Int) = flow {
        val response = client.submitForm(
            buildUrl { path(categoryReorderRequest()) },
            formParameters = Parameters.build {
                append("to", to.toString())
                append("from", from.toString())
            }
        ) {
            method = HttpMethod.Patch
            expectSuccess = true
        }
        emit(response)
    }.flowOn(Dispatchers.IO)

    fun deleteCategory(categoryId: Long) = flow {
        val response = client.delete(
            buildUrl { path(categoryDeleteRequest(categoryId)) },
        ) {
            expectSuccess = true
        }
        emit(response)
    }.flowOn(Dispatchers.IO)
    fun deleteCategory(category: Category) = deleteCategory(category.id)

    fun getMangaFromCategory(categoryId: Long) = flow {
        val response = client.get(
            buildUrl { path(getMangaInCategoryQuery(categoryId)) },
        ) {
            expectSuccess = true
        }.body<List<Manga>>()
        emit(response)
    }.flowOn(Dispatchers.IO)
    fun getMangaFromCategory(category: Category) = getMangaFromCategory(category.id)
}
