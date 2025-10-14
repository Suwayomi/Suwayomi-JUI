/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.data.updates

import ca.gosyer.jui.data.ApolloAppClient
import ca.gosyer.jui.data.chapter.ChapterRepositoryImpl.Companion.toMangaAndChapter
import ca.gosyer.jui.data.graphql.GetChapterUpdatesQuery
import ca.gosyer.jui.data.graphql.UpdateCategoryMutation
import ca.gosyer.jui.data.graphql.UpdateLibraryMutation
import ca.gosyer.jui.domain.server.Http
import ca.gosyer.jui.domain.updates.model.Updates
import ca.gosyer.jui.domain.updates.service.UpdatesRepository
import com.apollographql.apollo.ApolloClient
import io.ktor.http.Url
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UpdatesRepositoryImpl(
    private val apolloAppClient: ApolloAppClient,
    private val http: Http,
    private val serverUrl: Url,
) : UpdatesRepository {
    val apolloClient: ApolloClient
        get() = apolloAppClient.value

    override fun getRecentUpdates(pageNum: Int): Flow<Updates> =
        apolloClient.query(
            GetChapterUpdatesQuery(50, pageNum * 50),
        )
            .toFlow()
            .map {
                val data = it.dataAssertNoErrors
                Updates(
                    data.chapters.nodes.map { it.chapterWithMangaFragment.toMangaAndChapter() },
                    data.chapters.pageInfo.hasNextPage,
                )
            }

    override fun updateLibrary(): Flow<Unit> =
        apolloClient.mutation(
            UpdateLibraryMutation(),
        )
            .toFlow()
            .map {
                val data = it.dataAssertNoErrors
                data.updateLibraryManga!!.clientMutationId
            }

    override fun updateCategory(categoryId: Long): Flow<Unit> =
        apolloClient.mutation(
            UpdateCategoryMutation(listOf(categoryId.toInt())),
        )
            .toFlow()
            .map {
                val data = it.dataAssertNoErrors
                data.updateCategoryManga!!.clientMutationId
            }
}
