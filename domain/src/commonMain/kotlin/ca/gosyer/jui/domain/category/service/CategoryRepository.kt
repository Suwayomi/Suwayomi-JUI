/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.domain.category.service

import ca.gosyer.jui.domain.category.model.Category
import ca.gosyer.jui.domain.manga.model.Manga
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {

    fun getMangaCategories(
        mangaId: Long,
    ): Flow<List<Category>>

    fun addMangaToCategory(
        mangaId: Long,
        categoryId: Long,
    ): Flow<Unit>

    fun removeMangaFromCategory(
        mangaId: Long,
        categoryId: Long,
    ): Flow<Unit>

    fun getCategories(): Flow<List<Category>>

    fun createCategory(
        name: String,
    ): Flow<Unit>

    fun modifyCategory(
        categoryId: Long,
        name: String,
    ): Flow<Unit>

    fun reorderCategory(
        categoryId: Long,
        position: Int,
    ): Flow<Unit>

    fun deleteCategory(
        categoryId: Long,
    ): Flow<Unit>

    fun getMangaFromCategory(
        categoryId: Long,
    ): Flow<List<Manga>>

    fun updateCategoryMeta(
        categoryId: Long,
        key: String,
        value: String,
    ): Flow<Unit>
}
