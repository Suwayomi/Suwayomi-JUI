/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.domain.category.service

import ca.gosyer.jui.domain.category.model.Category
import ca.gosyer.jui.domain.manga.model.Manga
import io.ktor.client.statement.HttpResponse
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun getMangaCategories(mangaId: Long): Flow<List<Category>>
    fun addMangaToCategory(mangaId: Long, categoryId: Long): Flow<HttpResponse>
    fun removeMangaFromCategory(mangaId: Long, categoryId: Long): Flow<HttpResponse>
    fun getCategories(dropDefault: Boolean = false): Flow<List<Category>>
    fun createCategory(name: String): Flow<HttpResponse>
    fun modifyCategory(categoryId: Long, name: String? = null, isLanding: Boolean? = null): Flow<HttpResponse>
    fun reorderCategory(to: Int, from: Int): Flow<HttpResponse>
    fun deleteCategory(categoryId: Long): Flow<HttpResponse>
    fun getMangaFromCategory(categoryId: Long): Flow<List<Manga>>
}
