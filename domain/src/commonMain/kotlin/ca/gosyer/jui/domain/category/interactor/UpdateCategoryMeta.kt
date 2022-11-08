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
import kotlinx.coroutines.flow.flow
import me.tatarka.inject.annotations.Inject
import org.lighthousegames.logging.logging

class UpdateCategoryMeta @Inject constructor(private val categoryRepository: CategoryRepository) {

    suspend fun await(
        category: Category,
        example: Int = category.meta.example,
        onError: suspend (Throwable) -> Unit = {}
    ) = asFlow(category, example)
        .catch {
            onError(it)
            log.warn(it) { "Failed to update ${category.name}(${category.id}) meta" }
        }
        .collect()

    fun asFlow(
        category: Category,
        example: Int = category.meta.example
    ) = flow {
        if (example != category.meta.example) {
            categoryRepository.updateCategoryMeta(
                category.id,
                "example",
                example.toString()
            ).collect()
        }
        emit(Unit)
    }

    companion object {
        private val log = logging()
    }
}
