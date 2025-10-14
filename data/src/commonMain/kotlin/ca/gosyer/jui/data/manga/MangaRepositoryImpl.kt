package ca.gosyer.jui.data.manga

import ca.gosyer.jui.data.ApolloAppClient
import ca.gosyer.jui.data.graphql.GetMangaLibraryQuery
import ca.gosyer.jui.data.graphql.GetMangaQuery
import ca.gosyer.jui.data.graphql.GetThumbnailUrlQuery
import ca.gosyer.jui.data.graphql.RefreshMangaMutation
import ca.gosyer.jui.data.graphql.SetMangaMetaMutation
import ca.gosyer.jui.data.graphql.fragment.LibraryMangaFragment
import ca.gosyer.jui.data.graphql.fragment.MangaFragment
import ca.gosyer.jui.data.source.SourceRepositoryImpl.Companion.toSource
import ca.gosyer.jui.domain.manga.model.Manga
import ca.gosyer.jui.domain.manga.model.MangaMeta
import ca.gosyer.jui.domain.manga.model.MangaStatus
import ca.gosyer.jui.domain.manga.model.UpdateStrategy
import ca.gosyer.jui.domain.manga.service.MangaRepository
import ca.gosyer.jui.domain.server.Http
import com.apollographql.apollo.ApolloClient
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.Url
import io.ktor.utils.io.ByteReadChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ca.gosyer.jui.data.graphql.type.MangaStatus as GqlMangaStatus
import ca.gosyer.jui.data.graphql.type.UpdateStrategy as GqlUpdateStrategy

class MangaRepositoryImpl(
    private val apolloAppClient: ApolloAppClient,
    private val http: Http,
    private val serverUrl: Url,
) : MangaRepository {
    val apolloClient: ApolloClient
        get() = apolloAppClient.value

    override fun getManga(mangaId: Long): Flow<Manga> =
        apolloClient.query(
            GetMangaQuery(mangaId.toInt()),
        )
            .toFlow()
            .map {
                val data = it.dataAssertNoErrors
                data.manga.mangaFragment.toManga()
            }

    override fun refreshManga(mangaId: Long): Flow<Manga> =
        apolloClient.mutation(
            RefreshMangaMutation(mangaId.toInt()),
        )
            .toFlow()
            .map {
                val data = it.dataAssertNoErrors
                data.fetchManga!!.manga.mangaFragment.toManga()
            }

    override fun getMangaLibrary(mangaId: Long): Flow<Manga> =
        apolloClient.query(
            GetMangaLibraryQuery(mangaId.toInt()),
        )
            .toFlow()
            .map {
                val data = it.dataAssertNoErrors
                data.manga.libraryMangaFragment.toManga()
            }

    override fun getMangaThumbnail(
        mangaId: Long,
        block: HttpRequestBuilder.() -> Unit,
    ): Flow<ByteReadChannel> =
        apolloClient.query(
            GetThumbnailUrlQuery(mangaId.toInt()),
        )
            .toFlow()
            .map {
                val data = it.dataAssertNoErrors
                http.value.get(data.manga.thumbnailUrl!!).bodyAsChannel()
            }

    override fun updateMangaMeta(
        mangaId: Long,
        key: String,
        value: String,
    ): Flow<Unit> =
        apolloClient.mutation(
            SetMangaMetaMutation(mangaId.toInt(), key, value),
        )
            .toFlow()
            .map {
                val data = it.dataAssertNoErrors
                data.setMangaMeta!!.clientMutationId
            }

    companion object {
        internal fun MangaFragment.toManga(): Manga =
            Manga(
                id = id.toLong(),
                sourceId = sourceId,
                url = url,
                title = title,
                thumbnailUrl = thumbnailUrl,
                thumbnailUrlLastFetched = thumbnailUrlLastFetched ?: 0,
                initialized = initialized,
                artist = artist,
                author = author,
                description = description,
                genre = genre,
                status = when (status) {
                    GqlMangaStatus.ONGOING -> MangaStatus.ONGOING
                    GqlMangaStatus.COMPLETED -> MangaStatus.COMPLETED
                    GqlMangaStatus.LICENSED -> MangaStatus.LICENSED
                    GqlMangaStatus.PUBLISHING_FINISHED -> MangaStatus.PUBLISHING_FINISHED
                    GqlMangaStatus.CANCELLED -> MangaStatus.CANCELLED
                    GqlMangaStatus.ON_HIATUS -> MangaStatus.ON_HIATUS
                    GqlMangaStatus.UNKNOWN, GqlMangaStatus.UNKNOWN__ -> MangaStatus.UNKNOWN
                },
                inLibrary = inLibrary,
                source = null,
                updateStrategy = when (updateStrategy) {
                    GqlUpdateStrategy.ALWAYS_UPDATE -> UpdateStrategy.ALWAYS_UPDATE
                    GqlUpdateStrategy.ONLY_FETCH_ONCE -> UpdateStrategy.ONLY_FETCH_ONCE
                    GqlUpdateStrategy.UNKNOWN__ -> UpdateStrategy.ALWAYS_UPDATE
                },
                freshData = false,
                meta = MangaMeta(
                    meta.find { it.key == "juiReaderMode" }?.value.orEmpty(),
                ),
                realUrl = realUrl,
                lastFetchedAt = lastFetchedAt,
                chaptersLastFetchedAt = chaptersLastFetchedAt,
                inLibraryAt = inLibraryAt,
                unreadCount = null,
                downloadCount = null,
                chapterCount = null,
                lastChapterReadTime = null,
                age = age,
                chaptersAge = null,
            )

        internal fun LibraryMangaFragment.toManga(): Manga =
            Manga(
                id = id.toLong(),
                sourceId = sourceId,
                url = url,
                title = title,
                thumbnailUrl = thumbnailUrl,
                thumbnailUrlLastFetched = thumbnailUrlLastFetched ?: 0,
                initialized = initialized,
                artist = artist,
                author = author,
                description = description,
                genre = genre,
                status = when (status) {
                    GqlMangaStatus.ONGOING -> MangaStatus.ONGOING
                    GqlMangaStatus.COMPLETED -> MangaStatus.COMPLETED
                    GqlMangaStatus.LICENSED -> MangaStatus.LICENSED
                    GqlMangaStatus.PUBLISHING_FINISHED -> MangaStatus.PUBLISHING_FINISHED
                    GqlMangaStatus.CANCELLED -> MangaStatus.CANCELLED
                    GqlMangaStatus.ON_HIATUS -> MangaStatus.ON_HIATUS
                    GqlMangaStatus.UNKNOWN, GqlMangaStatus.UNKNOWN__ -> MangaStatus.UNKNOWN
                },
                inLibrary = inLibrary,
                source = source?.sourceFragment?.toSource(),
                updateStrategy = when (updateStrategy) {
                    GqlUpdateStrategy.ALWAYS_UPDATE -> UpdateStrategy.ALWAYS_UPDATE
                    GqlUpdateStrategy.ONLY_FETCH_ONCE -> UpdateStrategy.ONLY_FETCH_ONCE
                    GqlUpdateStrategy.UNKNOWN__ -> UpdateStrategy.ALWAYS_UPDATE
                },
                freshData = false,
                meta = MangaMeta(
                    meta.find { it.key == "juiReaderMode" }?.value.orEmpty(),
                ),
                realUrl = realUrl,
                lastFetchedAt = lastFetchedAt,
                chaptersLastFetchedAt = chaptersLastFetchedAt,
                inLibraryAt = inLibraryAt,
                unreadCount = unreadCount,
                downloadCount = downloadCount,
                chapterCount = chapters.totalCount,
                lastChapterReadTime = lastReadChapter?.lastReadAt ?: 0,
                age = age,
                chaptersAge = null,
            )
    }
}
