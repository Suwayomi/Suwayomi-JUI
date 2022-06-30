/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.data.category

import ca.gosyer.jui.core.lang.IO
import ca.gosyer.jui.data.base.BaseRepository
import ca.gosyer.jui.domain.category.model.Category
import ca.gosyer.jui.domain.category.service.CategoryRepository
import ca.gosyer.jui.domain.manga.model.Manga
import ca.gosyer.jui.domain.server.Http
import ca.gosyer.jui.domain.server.model.requests.addMangaToCategoryQuery
import ca.gosyer.jui.domain.server.model.requests.categoryDeleteRequest
import ca.gosyer.jui.domain.server.model.requests.categoryModifyRequest
import ca.gosyer.jui.domain.server.model.requests.categoryReorderRequest
import ca.gosyer.jui.domain.server.model.requests.createCategoryRequest
import ca.gosyer.jui.domain.server.model.requests.getCategoriesQuery
import ca.gosyer.jui.domain.server.model.requests.getMangaCategoriesQuery
import ca.gosyer.jui.domain.server.model.requests.getMangaInCategoryQuery
import ca.gosyer.jui.domain.server.model.requests.removeMangaFromCategoryRequest
import ca.gosyer.jui.domain.server.service.ServerPreferences
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

class CategoryRepositoryImpl @Inject constructor(
    client: Http,
    serverPreferences: ServerPreferences
) : BaseRepository(client, serverPreferences), CategoryRepository {

    override fun getMangaCategories(mangaId: Long) = flow {
        val response = client.get(
            buildUrl { path(getMangaCategoriesQuery(mangaId)) },
        ) {
            expectSuccess = true
        }.body<List<Category>>()
        emit(response)
    }.flowOn(Dispatchers.IO)

    override fun addMangaToCategory(mangaId: Long, categoryId: Long) = flow {
        val response = client.get(
            buildUrl { path(addMangaToCategoryQuery(mangaId, categoryId)) }
        ) {
            expectSuccess = true
        }
        emit(response)
    }.flowOn(Dispatchers.IO)

    override fun removeMangaFromCategory(mangaId: Long, categoryId: Long) = flow {
        val response = client.delete(
            buildUrl { path(removeMangaFromCategoryRequest(mangaId, categoryId)) }
        ) {
            expectSuccess = true
        }
        emit(response)
    }.flowOn(Dispatchers.IO)

    override fun getCategories(dropDefault: Boolean) = flow {
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

    override fun createCategory(name: String) = flow {
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

    override fun modifyCategory(categoryId: Long, name: String?, isLanding: Boolean?) = flow {
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

    override fun reorderCategory(to: Int, from: Int) = flow {
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

    override fun deleteCategory(categoryId: Long) = flow {
        val response = client.delete(
            buildUrl { path(categoryDeleteRequest(categoryId)) },
        ) {
            expectSuccess = true
        }
        emit(response)
    }.flowOn(Dispatchers.IO)

    override fun getMangaFromCategory(categoryId: Long) = flow {
        val response = client.get(
            buildUrl { path(getMangaInCategoryQuery(categoryId)) },
        ) {
            expectSuccess = true
        }.body<List<Manga>>()
        emit(response)
    }.flowOn(Dispatchers.IO)
}
