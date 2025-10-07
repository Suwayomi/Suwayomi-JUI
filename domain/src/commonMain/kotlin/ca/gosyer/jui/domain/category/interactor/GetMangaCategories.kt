/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.domain.category.interactor

import ca.gosyer.jui.domain.category.service.CategoryRepository
import ca.gosyer.jui.domain.manga.model.Manga
import com.diamondedge.logging.logging
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.singleOrNull
import me.tatarka.inject.annotations.Inject

@Inject
class GetMangaCategories(
    private val categoryRepository: CategoryRepository,
) {
    suspend fun await(
        mangaId: Long,
        onError: suspend (Throwable) -> Unit = {},
    ) = asFlow(mangaId)
        .catch {
            onError(it)
            log.warn(it) { "Failed to get categories for $mangaId" }
        }
        .singleOrNull()

    suspend fun await(
        manga: Manga,
        onError: suspend (Throwable) -> Unit = {},
    ) = asFlow(manga)
        .catch {
            onError(it)
            log.warn(it) { "Failed to get categories for ${manga.title}(${manga.id})" }
        }
        .singleOrNull()

    fun asFlow(mangaId: Long) = categoryRepository.getMangaCategories(mangaId)

    fun asFlow(manga: Manga) = categoryRepository.getMangaCategories(manga.id)

    companion object {
        private val log = logging()
    }
}
