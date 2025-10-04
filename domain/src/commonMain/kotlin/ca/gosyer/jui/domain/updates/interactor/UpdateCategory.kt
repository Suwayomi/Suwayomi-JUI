/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.domain.updates.interactor

import ca.gosyer.jui.domain.category.model.Category
import ca.gosyer.jui.domain.updates.service.UpdatesRepository
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import me.tatarka.inject.annotations.Inject
import org.lighthousegames.logging.logging

@Inject
class UpdateCategory(
    private val updatesRepository: UpdatesRepository,
) {
    suspend fun await(
        categoryId: Long,
        onError: suspend (Throwable) -> Unit = {},
    ) = asFlow(categoryId)
        .catch {
            onError(it)
            log.warn(it) { "Failed to update category $categoryId" }
        }
        .collect()

    suspend fun await(
        category: Category,
        onError: suspend (Throwable) -> Unit = {},
    ) = asFlow(category)
        .catch {
            onError(it)
            log.warn(it) { "Failed to update category ${category.name}(${category.id})" }
        }
        .collect()

    fun asFlow(categoryId: Long) = updatesRepository.updateCategory(categoryId)

    fun asFlow(category: Category) = updatesRepository.updateCategory(category.id)

    companion object {
        private val log = logging()
    }
}
