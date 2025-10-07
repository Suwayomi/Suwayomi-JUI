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
import com.diamondedge.logging.logging

@Inject
class CreateCategory(
    private val categoryRepository: CategoryRepository,
) {
    suspend fun await(
        name: String,
        onError: suspend (Throwable) -> Unit = {},
    ) = asFlow(name)
        .catch {
            onError(it)
            log.warn(it) { "Failed to create category $name" }
        }
        .collect()

    fun asFlow(name: String) = categoryRepository.createCategory(name)

    companion object {
        private val log = logging()
    }
}
