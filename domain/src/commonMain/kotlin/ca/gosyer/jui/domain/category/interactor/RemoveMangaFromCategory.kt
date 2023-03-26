/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.domain.category.interactor

import ca.gosyer.jui.domain.ServerListeners
import ca.gosyer.jui.domain.category.model.Category
import ca.gosyer.jui.domain.category.service.CategoryRepository
import ca.gosyer.jui.domain.manga.model.Manga
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import me.tatarka.inject.annotations.Inject
import org.lighthousegames.logging.logging

class RemoveMangaFromCategory @Inject constructor(
    private val categoryRepository: CategoryRepository,
    private val serverListeners: ServerListeners,
) {

    suspend fun await(mangaId: Long, categoryId: Long, onError: suspend (Throwable) -> Unit = {}) = asFlow(mangaId, categoryId)
        .catch {
            onError(it)
            log.warn(it) { "Failed to remove $mangaId from category $categoryId" }
        }
        .collect()

    suspend fun await(manga: Manga, category: Category, onError: suspend (Throwable) -> Unit = {}) = asFlow(manga, category)
        .catch {
            onError(it)
            log.warn(it) { "Failed to remove ${manga.title}(${manga.id}) from category ${category.name}" }
        }
        .collect()

    fun asFlow(mangaId: Long, categoryId: Long) = if (categoryId != 0L) {
        categoryRepository.removeMangaFromCategory(mangaId, categoryId)
            .map { serverListeners.updateCategoryManga(categoryId) }
    } else {
        flow {
            serverListeners.updateCategoryManga(categoryId)
            emit(Unit)
        }
    }

    fun asFlow(manga: Manga, category: Category) = if (category.id != 0L) {
        categoryRepository.removeMangaFromCategory(manga.id, category.id)
            .map { serverListeners.updateCategoryManga(category.id) }
    } else {
        flow {
            serverListeners.updateCategoryManga(category.id)
            emit(Unit)
        }
    }

    companion object {
        private val log = logging()
    }
}
