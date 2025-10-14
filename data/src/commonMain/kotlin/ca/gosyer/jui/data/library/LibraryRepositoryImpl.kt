package ca.gosyer.jui.data.library

import ca.gosyer.jui.data.ApolloAppClient
import ca.gosyer.jui.data.graphql.LibraryUpdateStatusChangedSubscription
import ca.gosyer.jui.data.graphql.LibraryUpdateStatusQuery
import ca.gosyer.jui.data.graphql.SetMangaInLibraryMutation
import ca.gosyer.jui.data.graphql.fragment.CategoryUpdateFragment
import ca.gosyer.jui.data.graphql.fragment.MangaUpdateFragment
import ca.gosyer.jui.data.graphql.fragment.UpdaterJobsInfoFragment
import ca.gosyer.jui.data.graphql.type.CategoryJobStatus
import ca.gosyer.jui.data.graphql.type.MangaJobStatus
import ca.gosyer.jui.domain.library.model.CategoryUpdate
import ca.gosyer.jui.domain.library.model.MangaUpdate
import ca.gosyer.jui.domain.library.model.UpdateStatus
import ca.gosyer.jui.domain.library.model.UpdaterJobsInfo
import ca.gosyer.jui.domain.library.model.UpdaterUpdates
import ca.gosyer.jui.domain.library.service.LibraryRepository
import ca.gosyer.jui.domain.server.Http
import com.apollographql.apollo.ApolloClient
import io.ktor.http.Url
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LibraryRepositoryImpl(
    private val apolloAppClient: ApolloAppClient,
    private val http: Http,
    private val serverUrl: Url,
) : LibraryRepository {
    val apolloClient: ApolloClient
        get() = apolloAppClient.value

    fun setMangaInLibrary(
        mangaId: Long,
        inLibrary: Boolean,
    ): Flow<Unit> =
        apolloClient.mutation(
            SetMangaInLibraryMutation(mangaId.toInt(), inLibrary),
        )
            .toFlow()
            .map {
                val data = it.dataAssertNoErrors
                data.updateManga!!.clientMutationId
            }

    override fun addMangaToLibrary(mangaId: Long): Flow<Unit> = setMangaInLibrary(mangaId, true)

    override fun removeMangaFromLibrary(mangaId: Long): Flow<Unit> = setMangaInLibrary(mangaId, false)

    override fun libraryUpdateSubscription(): Flow<UpdaterUpdates> =
        apolloClient.subscription(
            LibraryUpdateStatusChangedSubscription(),
        )
            .toFlow()
            .map {
                val data = it.dataAssertNoErrors.libraryUpdateStatusChanged
                UpdaterUpdates(
                    data.initial?.let {
                        UpdateStatus(
                            it.categoryUpdates.map {
                                it.categoryUpdateFragment.toCategoryUpdate()
                            },
                            it.mangaUpdates.map {
                                it.mangaUpdateFragment.toMangaUpdate()
                            },
                            it.jobsInfo.updaterJobsInfoFragment.toUpdaterJobsInfo(),
                        )
                    },
                    data.categoryUpdates.map {
                        it.categoryUpdateFragment.toCategoryUpdate()
                    },
                    data.mangaUpdates.map {
                        it.mangaUpdateFragment.toMangaUpdate()
                    },
                    data.jobsInfo.updaterJobsInfoFragment.toUpdaterJobsInfo(),
                    data.omittedUpdates,
                )
            }

    override fun libraryUpdateStatus(): Flow<UpdateStatus> =
        apolloClient.query(
            LibraryUpdateStatusQuery(),
        )
            .toFlow()
            .map {
                val data = it.dataAssertNoErrors.libraryUpdateStatus
                UpdateStatus(
                    data.categoryUpdates.map {
                        it.categoryUpdateFragment.toCategoryUpdate()
                    },
                    data.mangaUpdates.map {
                        it.mangaUpdateFragment.toMangaUpdate()
                    },
                    data.jobsInfo.updaterJobsInfoFragment.toUpdaterJobsInfo(),
                )
            }

    companion object {
        fun UpdaterJobsInfoFragment.toUpdaterJobsInfo() =
            UpdaterJobsInfo(
                finishedJobs,
                isRunning,
                skippedCategoriesCount,
                skippedMangasCount,
                totalJobs,
            )

        fun CategoryUpdateFragment.toCategoryUpdate() =
            CategoryUpdate(
                CategoryUpdate.UpdateCategory(
                    category.id.toLong(),
                    category.name,
                ),
                when (status) {
                    CategoryJobStatus.UPDATING -> CategoryUpdate.Status.UPDATING
                    CategoryJobStatus.SKIPPED -> CategoryUpdate.Status.SKIPPED
                    CategoryJobStatus.UNKNOWN__ -> CategoryUpdate.Status.UPDATING
                },
            )

        fun MangaUpdateFragment.toMangaUpdate() =
            MangaUpdate(
                MangaUpdate.UpdateManga(
                    manga.id.toLong(),
                    manga.title,
                ),
                when (status) {
                    MangaJobStatus.PENDING -> MangaUpdate.Status.PENDING
                    MangaJobStatus.RUNNING -> MangaUpdate.Status.RUNNING
                    MangaJobStatus.COMPLETE -> MangaUpdate.Status.COMPLETE
                    MangaJobStatus.FAILED -> MangaUpdate.Status.FAILED
                    MangaJobStatus.SKIPPED -> MangaUpdate.Status.SKIPPED
                    MangaJobStatus.UNKNOWN__ -> MangaUpdate.Status.FAILED
                },
            )
    }
}
