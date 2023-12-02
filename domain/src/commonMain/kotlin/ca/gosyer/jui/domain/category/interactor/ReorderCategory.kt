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

class ReorderCategory
    @Inject
    constructor(
        private val categoryRepository: CategoryRepository,
    ) {
        suspend fun await(
            to: Int,
            from: Int,
            onError: suspend (Throwable) -> Unit = {},
        ) = asFlow(to, from)
            .catch {
                onError(it)
                log.warn(it) { "Failed to move category from $from to $to" }
            }
            .collect()

        fun asFlow(
            to: Int,
            from: Int,
        ) = categoryRepository.reorderCategory(to, from)

        companion object {
            private val log = logging()
        }
    }
