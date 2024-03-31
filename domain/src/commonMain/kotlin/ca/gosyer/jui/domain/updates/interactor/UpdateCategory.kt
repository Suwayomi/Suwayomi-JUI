/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.domain.updates.interactor

import ca.gosyer.jui.domain.category.model.Category
import ca.gosyer.jui.domain.updates.service.UpdatesRepositoryOld
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import me.tatarka.inject.annotations.Inject
import org.lighthousegames.logging.logging

class UpdateCategory
    @Inject
    constructor(
        private val updatesRepositoryOld: UpdatesRepositoryOld,
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

        fun asFlow(categoryId: Long) = updatesRepositoryOld.updateCategory(categoryId)

        fun asFlow(category: Category) = updatesRepositoryOld.updateCategory(category.id)

        companion object {
            private val log = logging()
        }
    }
