/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.domain.category.interactor

import ca.gosyer.jui.domain.category.service.CategoryRepository
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import me.tatarka.inject.annotations.Inject
import org.lighthousegames.logging.logging

@Inject
class ReorderCategory(
    private val categoryRepository: CategoryRepository,
) {
    suspend fun await(
        categoryId: Long,
        position: Int,
        onError: suspend (Throwable) -> Unit = {},
    ) = asFlow(categoryId, position)
        .catch {
            onError(it)
            log.warn(it) { "Failed to move category $categoryId to $position" }
        }
        .collect()

    fun asFlow(
        categoryId: Long,
        position: Int,
    ) = categoryRepository.reorderCategory(categoryId, position)

    companion object {
        private val log = logging()
    }
}
