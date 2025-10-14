/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.domain.category.interactor

import ca.gosyer.jui.domain.ServerListeners
import ca.gosyer.jui.domain.category.service.CategoryRepository
import com.diamondedge.logging.logging
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.singleOrNull
import me.tatarka.inject.annotations.Inject

@Inject
class GetCategories(
    private val categoryRepository: CategoryRepository,
    private val serverListeners: ServerListeners,
) {
    suspend fun await(
        dropDefault: Boolean = false,
        onError: suspend (Throwable) -> Unit = {},
    ) = asFlow(dropDefault)
        .catch {
            onError(it)
            log.warn(it) { "Failed to get categories" }
        }
        .singleOrNull()

    fun asFlow(dropDefault: Boolean = false) = serverListeners.combineCategoryManga(
        categoryRepository.getCategories()
    ).map { categories ->
        if (dropDefault) {
            categories.filterNot { it.name.equals("default", true) }
        } else {
            categories
        }
    }


    companion object {
        private val log = logging()
    }
}
