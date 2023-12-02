/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.domain.category.interactor

import ca.gosyer.jui.domain.category.model.Category
import ca.gosyer.jui.domain.category.service.CategoryRepository
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import me.tatarka.inject.annotations.Inject
import org.lighthousegames.logging.logging

class DeleteCategory
    @Inject
    constructor(
        private val categoryRepository: CategoryRepository,
    ) {
        suspend fun await(
            categoryId: Long,
            onError: suspend (Throwable) -> Unit = {},
        ) = asFlow(categoryId)
            .catch {
                onError(it)
                log.warn(it) { "Failed to delete category $categoryId" }
            }
            .collect()

        suspend fun await(
            category: Category,
            onError: suspend (Throwable) -> Unit = {},
        ) = asFlow(category)
            .catch {
                onError(it)
                log.warn(it) { "Failed to delete category ${category.name}" }
            }
            .collect()

        fun asFlow(categoryId: Long) = categoryRepository.deleteCategory(categoryId)

        fun asFlow(category: Category) = categoryRepository.deleteCategory(category.id)

        companion object {
            private val log = logging()
        }
    }
