/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.domain.category.interactor

import ca.gosyer.jui.domain.category.model.Category
import ca.gosyer.jui.domain.category.service.CategoryRepository
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.singleOrNull
import me.tatarka.inject.annotations.Inject
import org.lighthousegames.logging.logging

class GetMangaListFromCategory @Inject constructor(private val categoryRepository: CategoryRepository) {

    suspend fun await(categoryId: Long) = asFlow(categoryId)
        .catch { log.warn(it) { "Failed to get manga list from category $categoryId" } }
        .singleOrNull()

    suspend fun await(category: Category) = asFlow(category)
        .catch { log.warn(it) { "Failed to get manga list from category ${category.name}" } }
        .singleOrNull()

    fun asFlow(categoryId: Long) = categoryRepository.getMangaFromCategory(categoryId)

    fun asFlow(category: Category) = categoryRepository.getMangaFromCategory(category.id)

    companion object {
        private val log = logging()
    }
}
