/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.data.download

import ca.gosyer.jui.data.graphql.ClearDownloaderMutation
import ca.gosyer.jui.data.graphql.DequeueChapterDownloadMutation
import ca.gosyer.jui.data.graphql.EnqueueChapterDownloadMutation
import ca.gosyer.jui.data.graphql.EnqueueChapterDownloadsMutation
import ca.gosyer.jui.data.graphql.ReorderChapterDownloadMutation
import ca.gosyer.jui.data.graphql.StartDownloaderMutation
import ca.gosyer.jui.data.graphql.StopDownloaderMutation
import ca.gosyer.jui.domain.download.service.DownloadRepository
import ca.gosyer.jui.domain.server.Http
import com.apollographql.apollo.ApolloClient
import io.ktor.http.Url
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DownloadRepositoryImpl(
    private val apolloClient: ApolloClient,
    private val http: Http,
    private val serverUrl: Url,
): DownloadRepository {
    override fun startDownloading(): Flow<Unit> {
        return apolloClient.mutation(
            StartDownloaderMutation()
        )
            .toFlow()
            .map {
                it.dataAssertNoErrors
            }
    }

    override fun stopDownloading(): Flow<Unit> {
        return apolloClient.mutation(
            StopDownloaderMutation()
        )
            .toFlow()
            .map {
                it.dataAssertNoErrors
            }
    }

    override fun clearDownloadQueue(): Flow<Unit> {
        return apolloClient.mutation(
            ClearDownloaderMutation()
        )
            .toFlow()
            .map {
                it.dataAssertNoErrors
            }
    }

    override fun queueChapterDownload(chapterId: Long): Flow<Unit> {
        return apolloClient.mutation(
            EnqueueChapterDownloadMutation(chapterId.toInt())
        )
            .toFlow()
            .map {
                it.dataAssertNoErrors
            }
    }

    override fun stopChapterDownload(chapterId: Long): Flow<Unit> {
        return apolloClient.mutation(
            DequeueChapterDownloadMutation(chapterId.toInt())
        )
            .toFlow()
            .map {
                it.dataAssertNoErrors
            }
    }

    override fun reorderChapterDownload(
        chapterId: Long,
        to: Int,
    ): Flow<Unit> {
        return apolloClient.mutation(
            ReorderChapterDownloadMutation(chapterId.toInt(), to)
        )
            .toFlow()
            .map {
                it.dataAssertNoErrors
            }
    }

    override fun batchDownload(chapterIds: List<Long>): Flow<Unit> {
        return apolloClient.mutation(
            EnqueueChapterDownloadsMutation(chapterIds.map { it.toInt() })
        )
            .toFlow()
            .map {
                it.dataAssertNoErrors
            }
    }


}
