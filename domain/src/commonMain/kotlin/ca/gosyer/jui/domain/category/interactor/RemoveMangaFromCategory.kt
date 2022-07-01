/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.domain.category.interactor

import ca.gosyer.jui.domain.category.model.Category
import ca.gosyer.jui.domain.category.service.CategoryRepository
import ca.gosyer.jui.domain.manga.model.Manga
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import me.tatarka.inject.annotations.Inject
import org.lighthousegames.logging.logging

class RemoveMangaFromCategory @Inject constructor(private val categoryRepository: CategoryRepository) {

    suspend fun await(mangaId: Long, categoryId: Long) = asFlow(mangaId, categoryId)
        .catch { log.warn(it) { "Failed to remove $mangaId from category $categoryId" } }
        .collect()

    suspend fun await(manga: Manga, category: Category) = asFlow(manga, category)
        .catch { log.warn(it) { "Failed to remove ${manga.title}(${manga.id}) from category ${category.name}" } }
        .collect()

    fun asFlow(mangaId: Long, categoryId: Long) = categoryRepository.removeMangaFromCategory(mangaId, categoryId)

    fun asFlow(manga: Manga, category: Category) = categoryRepository.removeMangaFromCategory(manga.id, category.id)

    companion object {
        private val log = logging()
    }
}
