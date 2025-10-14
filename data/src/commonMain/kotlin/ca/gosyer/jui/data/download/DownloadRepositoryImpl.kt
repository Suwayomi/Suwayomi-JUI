/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.data.download

import ca.gosyer.jui.data.ApolloAppClient
import ca.gosyer.jui.data.graphql.ClearDownloaderMutation
import ca.gosyer.jui.data.graphql.DequeueChapterDownloadMutation
import ca.gosyer.jui.data.graphql.DownloadStatusChangedSubscription
import ca.gosyer.jui.data.graphql.DownloadStatusQuery
import ca.gosyer.jui.data.graphql.EnqueueChapterDownloadMutation
import ca.gosyer.jui.data.graphql.EnqueueChapterDownloadsMutation
import ca.gosyer.jui.data.graphql.ReorderChapterDownloadMutation
import ca.gosyer.jui.data.graphql.StartDownloaderMutation
import ca.gosyer.jui.data.graphql.StopDownloaderMutation
import ca.gosyer.jui.data.graphql.fragment.DownloadFragment
import ca.gosyer.jui.domain.download.model.DownloadChapter
import ca.gosyer.jui.domain.download.model.DownloadManga
import ca.gosyer.jui.domain.download.model.DownloadQueueItem
import ca.gosyer.jui.domain.download.model.DownloadState
import ca.gosyer.jui.domain.download.model.DownloadStatus
import ca.gosyer.jui.domain.download.model.DownloadUpdate
import ca.gosyer.jui.domain.download.model.DownloadUpdateType
import ca.gosyer.jui.domain.download.model.DownloadUpdates
import ca.gosyer.jui.domain.download.model.DownloaderState
import ca.gosyer.jui.domain.download.service.DownloadRepository
import ca.gosyer.jui.domain.server.Http
import com.apollographql.apollo.ApolloClient
import io.ktor.http.Url
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ca.gosyer.jui.data.graphql.type.DownloadState as GraphQLDownloadState
import ca.gosyer.jui.data.graphql.type.DownloadUpdateType as GraphQLDownloadUpdateType
import ca.gosyer.jui.data.graphql.type.DownloaderState as GraphQLDownloaderState

class DownloadRepositoryImpl(
    private val apolloAppClient: ApolloAppClient,
    private val http: Http,
    private val serverUrl: Url,
) : DownloadRepository {
    val apolloClient: ApolloClient
        get() = apolloAppClient.value

    override fun startDownloading(): Flow<Unit> =
        apolloClient.mutation(
            StartDownloaderMutation(),
        )
            .toFlow()
            .map {
                it.dataAssertNoErrors
            }

    override fun stopDownloading(): Flow<Unit> =
        apolloClient.mutation(
            StopDownloaderMutation(),
        )
            .toFlow()
            .map {
                it.dataAssertNoErrors
            }

    override fun clearDownloadQueue(): Flow<Unit> =
        apolloClient.mutation(
            ClearDownloaderMutation(),
        )
            .toFlow()
            .map {
                it.dataAssertNoErrors
            }

    override fun queueChapterDownload(chapterId: Long): Flow<Unit> =
        apolloClient.mutation(
            EnqueueChapterDownloadMutation(chapterId.toInt()),
        )
            .toFlow()
            .map {
                it.dataAssertNoErrors
            }

    override fun stopChapterDownload(chapterId: Long): Flow<Unit> =
        apolloClient.mutation(
            DequeueChapterDownloadMutation(chapterId.toInt()),
        )
            .toFlow()
            .map {
                it.dataAssertNoErrors
            }

    override fun reorderChapterDownload(
        chapterId: Long,
        to: Int,
    ): Flow<Unit> =
        apolloClient.mutation(
            ReorderChapterDownloadMutation(chapterId.toInt(), to),
        )
            .toFlow()
            .map {
                it.dataAssertNoErrors
            }

    override fun batchDownload(chapterIds: List<Long>): Flow<Unit> =
        apolloClient.mutation(
            EnqueueChapterDownloadsMutation(chapterIds.map { it.toInt() }),
        )
            .toFlow()
            .map {
                it.dataAssertNoErrors
            }

    override fun downloadSubscription(): Flow<DownloadUpdates> =
        apolloClient.subscription(
            DownloadStatusChangedSubscription(),
        )
            .toFlow()
            .map {
                val data = it.dataAssertNoErrors.downloadStatusChanged

                DownloadUpdates(
                    data.initial?.map { it.downloadFragment.toDownloadQueueItem() },
                    data.omittedUpdates,
                    data.state.toClient(),
                    data.updates.map {
                        DownloadUpdate(
                            when (it.type) {
                                GraphQLDownloadUpdateType.QUEUED -> DownloadUpdateType.QUEUED
                                GraphQLDownloadUpdateType.DEQUEUED -> DownloadUpdateType.DEQUEUED
                                GraphQLDownloadUpdateType.PAUSED -> DownloadUpdateType.PAUSED
                                GraphQLDownloadUpdateType.STOPPED -> DownloadUpdateType.STOPPED
                                GraphQLDownloadUpdateType.PROGRESS -> DownloadUpdateType.PROGRESS
                                GraphQLDownloadUpdateType.FINISHED -> DownloadUpdateType.FINISHED
                                GraphQLDownloadUpdateType.ERROR -> DownloadUpdateType.ERROR
                                GraphQLDownloadUpdateType.POSITION -> DownloadUpdateType.POSITION
                                GraphQLDownloadUpdateType.UNKNOWN__ -> null
                            },
                            it.download.downloadFragment.toDownloadQueueItem(),
                        )
                    },
                )
            }

    override fun downloadStatus(): Flow<DownloadStatus> =
        apolloClient.query(
            DownloadStatusQuery(),
        )
            .toFlow()
            .map {
                val data = it.dataAssertNoErrors.downloadStatus
                DownloadStatus(
                    data.state.toClient(),
                    data.queue.map { it.downloadFragment.toDownloadQueueItem() },
                )
            }

    companion object {
        fun DownloadFragment.toDownloadQueueItem(): DownloadQueueItem =
            DownloadQueueItem(
                position = this.position,
                progress = this.progress.toFloat(),
                state = when (this.state) {
                    GraphQLDownloadState.QUEUED -> DownloadState.QUEUED
                    GraphQLDownloadState.DOWNLOADING -> DownloadState.DOWNLOADING
                    GraphQLDownloadState.FINISHED -> DownloadState.FINISHED
                    GraphQLDownloadState.ERROR -> DownloadState.ERROR
                    GraphQLDownloadState.UNKNOWN__ -> DownloadState.ERROR
                },
                tries = this.tries,
                chapter = DownloadChapter(
                    chapter.id.toLong(),
                    chapter.name,
                    chapter.pageCount,
                ),
                manga = DownloadManga(
                    manga.id.toLong(),
                    manga.title,
                    manga.thumbnailUrl,
                    manga.thumbnailUrlLastFetched ?: 0,
                ),
            )

        fun GraphQLDownloaderState.toClient(): DownloaderState =
            when (this) {
                GraphQLDownloaderState.STARTED -> DownloaderState.STARTED
                GraphQLDownloaderState.STOPPED -> DownloaderState.STOPPED
                GraphQLDownloaderState.UNKNOWN__ -> DownloaderState.STOPPED
            }
    }
}
