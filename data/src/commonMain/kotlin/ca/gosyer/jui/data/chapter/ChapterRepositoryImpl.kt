package ca.gosyer.jui.data.chapter

import ca.gosyer.jui.data.graphql.DeleteDownloadedChapterMutation
import ca.gosyer.jui.data.graphql.DeleteDownloadedChaptersMutation
import ca.gosyer.jui.data.graphql.FetchChapterPagesMutation
import ca.gosyer.jui.data.graphql.FetchChaptersMutation
import ca.gosyer.jui.data.graphql.GetChapterQuery
import ca.gosyer.jui.data.graphql.GetMangaChaptersQuery
import ca.gosyer.jui.data.graphql.UpdateChapterMetaMutation
import ca.gosyer.jui.data.graphql.UpdateChapterMutation
import ca.gosyer.jui.data.graphql.UpdateChaptersMutation
import ca.gosyer.jui.data.graphql.fragment.ChapterFragment
import ca.gosyer.jui.data.graphql.fragment.ChapterWithMangaFragment
import ca.gosyer.jui.data.graphql.type.UpdateChapterPatchInput
import ca.gosyer.jui.data.manga.MangaRepositoryImpl.Companion.toManga
import ca.gosyer.jui.domain.chapter.model.Chapter
import ca.gosyer.jui.domain.chapter.model.ChapterMeta
import ca.gosyer.jui.domain.chapter.service.ChapterRepository
import ca.gosyer.jui.domain.server.Http
import ca.gosyer.jui.domain.updates.model.MangaAndChapter
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.statement.readBytes
import io.ktor.http.Url
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

class ChapterRepositoryImpl(
    private val apolloClient: ApolloClient,
    private val http: Http,
    private val serverUrl: Url,
) : ChapterRepository {
    override fun getChapter(chapterId: Long): Flow<Chapter> =
        apolloClient.query(
            GetChapterQuery(chapterId.toInt()),
        )
            .toFlow()
            .map {
                val data = it.dataAssertNoErrors
                data.chapter.chapterFragment.toChapter()
            }

    override fun getChapters(mangaId: Long): Flow<List<Chapter>> =
        apolloClient.query(
            GetMangaChaptersQuery(mangaId.toInt()),
        )
            .toFlow()
            .map {
                val data = it.dataAssertNoErrors
                data.chapters.nodes.map { it.chapterFragment.toChapter() }
            }

    override fun updateChapter(
        chapterId: Long,
        bookmarked: Boolean?,
        read: Boolean?,
        lastPageRead: Int?,
    ): Flow<Unit> =
        apolloClient.mutation(
            UpdateChapterMutation(
                chapterId.toInt(),
                UpdateChapterPatchInput(
                    isBookmarked = Optional.presentIfNotNull(bookmarked),
                    isRead = Optional.presentIfNotNull(read),
                    lastPageRead = Optional.presentIfNotNull(lastPageRead),
                ),
            ),
        )
            .toFlow()
            .map {
                it.dataAssertNoErrors
            }

    override fun updateChapters(
        chapterIds: List<Long>,
        bookmarked: Boolean?,
        read: Boolean?,
        lastPageRead: Int?,
    ): Flow<Unit> =
        apolloClient.mutation(
            UpdateChaptersMutation(
                chapterIds.map { it.toInt() },
                UpdateChapterPatchInput(
                    isBookmarked = Optional.presentIfNotNull(bookmarked),
                    isRead = Optional.presentIfNotNull(read),
                    lastPageRead = Optional.presentIfNotNull(lastPageRead),
                ),
            ),
        )
            .toFlow()
            .map {
                it.dataAssertNoErrors
            }

    override fun deleteDownloadedChapter(chapterId: Long): Flow<Unit> =
        apolloClient.mutation(
            DeleteDownloadedChapterMutation(
                chapterId.toInt(),
            ),
        )
            .toFlow()
            .map {
                it.dataAssertNoErrors
            }

    override fun deleteDownloadedChapters(chapterIds: List<Long>): Flow<Unit> =
        apolloClient.mutation(
            DeleteDownloadedChaptersMutation(
                chapterIds.map { it.toInt() },
            ),
        )
            .toFlow()
            .map {
                it.dataAssertNoErrors
            }

    override fun updateChapterMeta(
        chapterId: Long,
        key: String,
        value: String,
    ): Flow<Unit> =
        apolloClient.mutation(
            UpdateChapterMetaMutation(
                chapterId.toInt(),
                key,
                value,
            ),
        )
            .toFlow()
            .map {
                it.dataAssertNoErrors
            }

    override fun fetchChapters(mangaId: Long): Flow<List<Chapter>> =
        apolloClient.mutation(
            FetchChaptersMutation(
                mangaId.toInt(),
            ),
        )
            .toFlow()
            .map {
                val chapters = it.dataAssertNoErrors
                chapters.fetchChapters!!.chapters.map { it.chapterFragment.toChapter() }
            }

    override fun getPages(chapterId: Long): Flow<List<String>> =
        apolloClient.mutation(
            FetchChapterPagesMutation(
                chapterId.toInt(),
            ),
        )
            .toFlow()
            .map {
                val chapters = it.dataAssertNoErrors
                chapters.fetchChapterPages!!.pages
            }

    override fun getPage(
        url: String,
        block: HttpRequestBuilder.() -> Unit,
    ): Flow<ByteArray> {
        val realUrl = Url("$serverUrl$url")

        return flow { http.get(realUrl, block).readBytes() }
    }

    companion object {
        internal fun ChapterFragment.toChapter(): Chapter =
            Chapter(
                id = id.toLong(),
                url = url,
                name = name,
                uploadDate = uploadDate,
                chapterNumber = chapterNumber.toFloat(),
                scanlator = scanlator,
                mangaId = mangaId.toLong(),
                read = isRead,
                bookmarked = isBookmarked,
                lastPageRead = lastPageRead,
                index = sourceOrder,
                fetchedAt = fetchedAt,
                realUrl = realUrl,
                pageCount = pageCount,
                lastReadAt = lastPageRead,
                downloaded = isDownloaded,
                meta = ChapterMeta(
                    juiPageOffset = meta.find { it.key == "juiPageOffset" }?.value?.toIntOrNull() ?: 0,
                ),
            )

        internal fun ChapterWithMangaFragment.toMangaAndChapter(): MangaAndChapter =
            MangaAndChapter(
                manga.mangaFragment.toManga(),
                chapterFragment.toChapter(),
            )
    }
}
