/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.data.category

import ca.gosyer.jui.data.graphql.AddMangaToCategoriesMutation
import ca.gosyer.jui.data.graphql.CreateCategoryMutation
import ca.gosyer.jui.data.graphql.DeleteCategoryMutation
import ca.gosyer.jui.data.graphql.GetCategoriesQuery
import ca.gosyer.jui.data.graphql.GetCategoryMangaQuery
import ca.gosyer.jui.data.graphql.GetMangaCategoriesQuery
import ca.gosyer.jui.data.graphql.ModifyCategoryMutation
import ca.gosyer.jui.data.graphql.RemoveMangaFromCategoriesMutation
import ca.gosyer.jui.data.graphql.ReorderCategoryMutation
import ca.gosyer.jui.data.graphql.SetCategoryMetaMutation
import ca.gosyer.jui.data.graphql.fragment.CategoryFragment
import ca.gosyer.jui.data.manga.MangaRepositoryImpl.Companion.toManga
import ca.gosyer.jui.domain.category.model.Category
import ca.gosyer.jui.domain.category.model.CategoryMeta
import ca.gosyer.jui.domain.category.service.CategoryRepository
import ca.gosyer.jui.domain.manga.model.Manga
import ca.gosyer.jui.domain.server.Http
import com.apollographql.apollo.ApolloClient
import io.ktor.http.Url
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CategoryRepositoryImpl(
    private val apolloClient: ApolloClient,
    private val http: Http,
    private val serverUrl: Url,
): CategoryRepository {
    override fun getMangaCategories(mangaId: Long): Flow<List<Category>> {
        return apolloClient.query(
            GetMangaCategoriesQuery(mangaId.toInt())
        )
            .toFlow()
            .map {
                val data = it.dataAssertNoErrors
                data.manga.categories.nodes.map { it.categoryFragment.toCategory() }
            }
    }

    override fun addMangaToCategory(
        mangaId: Long,
        categoryId: Long,
    ): Flow<Unit> {
        return apolloClient.mutation(
            AddMangaToCategoriesMutation(mangaId.toInt(), listOf(categoryId.toInt()))
        )
            .toFlow()
            .map {
                val data = it.dataAssertNoErrors
                data.updateMangaCategories!!.clientMutationId
            }
    }

    override fun removeMangaFromCategory(
        mangaId: Long,
        categoryId: Long,
    ): Flow<Unit> {
        return apolloClient.mutation(
            RemoveMangaFromCategoriesMutation(mangaId.toInt(), listOf(categoryId.toInt()))
        )
            .toFlow()
            .map {
                val data = it.dataAssertNoErrors
                data.updateMangaCategories!!.clientMutationId
            }
    }

    override fun getCategories(): Flow<List<Category>> {
        return apolloClient.query(
            GetCategoriesQuery()
        )
            .toFlow()
            .map {
                val data = it.dataAssertNoErrors
                data.categories.nodes.map { it.categoryFragment.toCategory() }
            }
    }

    override fun createCategory(name: String): Flow<Unit> {
        return apolloClient.mutation(
            CreateCategoryMutation(name)
        )
            .toFlow()
            .map {
                val data = it.dataAssertNoErrors
                data.createCategory!!.clientMutationId
            }
    }

    override fun modifyCategory(
        categoryId: Long,
        name: String,
    ): Flow<Unit> {
        return apolloClient.mutation(
            ModifyCategoryMutation(categoryId.toInt(), name)
        )
            .toFlow()
            .map {
                val data = it.dataAssertNoErrors
                data.updateCategory!!.clientMutationId
            }
    }

    override fun reorderCategory(
        categoryId: Long,
        position: Int,
    ): Flow<Unit> {
        return apolloClient.mutation(
            ReorderCategoryMutation(categoryId.toInt(), position)
        )
            .toFlow()
            .map {
                val data = it.dataAssertNoErrors
                data.updateCategoryOrder!!.clientMutationId
            }
    }

    override fun deleteCategory(categoryId: Long): Flow<Unit> {
        return apolloClient.mutation(
            DeleteCategoryMutation(categoryId.toInt())
        )
            .toFlow()
            .map {
                val data = it.dataAssertNoErrors
                data.deleteCategory!!.clientMutationId
            }
    }

    override fun getMangaFromCategory(categoryId: Long): Flow<List<Manga>> {
        return apolloClient.query(
            GetCategoryMangaQuery(listOf(categoryId.toInt()))
        )
            .toFlow()
            .map {
                val data = it.dataAssertNoErrors
                data.mangas.nodes.map { it.mangaFragment.toManga() }
            }
    }

    override fun updateCategoryMeta(
        categoryId: Long,
        key: String,
        value: String,
    ): Flow<Unit> {
        return apolloClient.mutation(
            SetCategoryMetaMutation(categoryId.toInt(), key, value)
        )
            .toFlow()
            .map {
                val data = it.dataAssertNoErrors
                data.setCategoryMeta!!.clientMutationId
            }
    }

    companion object {
        internal fun CategoryFragment.toCategory(): Category {
            return Category(
                id = id.toLong(),
                order = order,
                name = name,
                default = default,
                meta = CategoryMeta()
            )
        }
    }
}
