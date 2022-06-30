/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.data.server.interactions

import ca.gosyer.jui.core.lang.IO
import ca.gosyer.jui.data.models.Chapter
import ca.gosyer.jui.data.models.Manga
import ca.gosyer.jui.data.server.Http
import ca.gosyer.jui.data.server.ServerPreferences
import ca.gosyer.jui.data.server.requests.deleteDownloadedChapterRequest
import ca.gosyer.jui.data.server.requests.getChapterQuery
import ca.gosyer.jui.data.server.requests.getMangaChaptersQuery
import ca.gosyer.jui.data.server.requests.getPageQuery
import ca.gosyer.jui.data.server.requests.queueDownloadChapterRequest
import ca.gosyer.jui.data.server.requests.stopDownloadingChapterRequest
import ca.gosyer.jui.data.server.requests.updateChapterMetaRequest
import ca.gosyer.jui.data.server.requests.updateChapterRequest
import io.ktor.client.call.body
import io.ktor.client.plugins.expectSuccess
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.delete
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.get
import io.ktor.http.HttpMethod
import io.ktor.http.Parameters
import io.ktor.http.path
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import me.tatarka.inject.annotations.Inject

class ChapterInteractionHandler @Inject constructor(
    client: Http,
    serverPreferences: ServerPreferences
) : BaseInteractionHandler(client, serverPreferences) {

    fun getChapters(mangaId: Long, refresh: Boolean = false) = flow {
        val response = client.get(
            buildUrl {
                path(getMangaChaptersQuery(mangaId))
                if (refresh) {
                    parameter("onlineFetch", true)
                }
            }
        ) {
            expectSuccess = true
        }.body<List<Chapter>>()
        emit(response)
    }.flowOn(Dispatchers.IO)

    fun getChapters(manga: Manga, refresh: Boolean = false) = getChapters(manga.id, refresh)

    fun getChapter(mangaId: Long, chapterIndex: Int) = flow {
        val response = client.get(
            buildUrl { path(getChapterQuery(mangaId, chapterIndex)) }
        ) {
            expectSuccess = true
        }.body<Chapter>()
        emit(response)
    }.flowOn(Dispatchers.IO)

    fun getChapter(chapter: Chapter) = getChapter(chapter.mangaId, chapter.index)

    fun getChapter(manga: Manga, chapterIndex: Int) = getChapter(manga.id, chapterIndex)

    fun getChapter(manga: Manga, chapter: Chapter) = getChapter(manga.id, chapter.index)

    fun updateChapter(
        mangaId: Long,
        chapterIndex: Int,
        read: Boolean? = null,
        bookmarked: Boolean? = null,
        lastPageRead: Int? = null,
        markPreviousRead: Boolean? = null
    ) = flow {
        val response = client.submitForm(
            buildUrl { path(updateChapterRequest(mangaId, chapterIndex)) },
            formParameters = Parameters.build {
                if (read != null) {
                    append("read", read.toString())
                }
                if (bookmarked != null) {
                    append("bookmarked", bookmarked.toString())
                }
                if (lastPageRead != null) {
                    append("lastPageRead", lastPageRead.toString())
                }
                if (markPreviousRead != null) {
                    append("markPrevRead", markPreviousRead.toString())
                }
            }
        ) {
            method = HttpMethod.Patch
            expectSuccess = true
        }
        emit(response)
    }.flowOn(Dispatchers.IO)

    fun updateChapter(
        manga: Manga,
        chapterIndex: Int,
        read: Boolean? = null,
        bookmarked: Boolean? = null,
        lastPageRead: Int? = null,
        markPreviousRead: Boolean? = null
    ) = updateChapter(
        manga.id,
        chapterIndex,
        read,
        bookmarked,
        lastPageRead,
        markPreviousRead
    )

    fun updateChapter(
        manga: Manga,
        chapter: Chapter,
        read: Boolean? = null,
        bookmarked: Boolean? = null,
        lastPageRead: Int? = null,
        markPreviousRead: Boolean? = null
    ) = updateChapter(
        manga.id,
        chapter.index,
        read,
        bookmarked,
        lastPageRead,
        markPreviousRead
    )

    fun getPage(mangaId: Long, chapterIndex: Int, pageNum: Int, block: HttpRequestBuilder.() -> Unit) = flow {
        val response = client.get(
            buildUrl { path(getPageQuery(mangaId, chapterIndex, pageNum)) }
        ) {
            expectSuccess = true
            block()
        }
        emit(response)
    }.flowOn(Dispatchers.IO)

    fun getPage(chapter: Chapter, pageNum: Int, block: HttpRequestBuilder.() -> Unit) = getPage(chapter.mangaId, chapter.index, pageNum, block)

    fun getPage(manga: Manga, chapterIndex: Int, pageNum: Int, block: HttpRequestBuilder.() -> Unit) = getPage(manga.id, chapterIndex, pageNum, block)

    fun getPage(manga: Manga, chapter: Chapter, pageNum: Int, block: HttpRequestBuilder.() -> Unit) = getPage(manga.id, chapter.index, pageNum, block)

    fun deleteChapterDownload(mangaId: Long, chapterIndex: Int) = flow {
        val response = client.delete(
            buildUrl { path(deleteDownloadedChapterRequest(mangaId, chapterIndex)) }
        ) {
            expectSuccess = true
        }
        emit(response)
    }.flowOn(Dispatchers.IO)

    fun deleteChapterDownload(chapter: Chapter) = deleteChapterDownload(chapter.mangaId, chapter.index)

    fun deleteChapterDownload(manga: Manga, chapterIndex: Int) = deleteChapterDownload(manga.id, chapterIndex)

    fun deleteChapterDownload(manga: Manga, chapter: Chapter) = deleteChapterDownload(manga.id, chapter.index)

    fun queueChapterDownload(mangaId: Long, chapterIndex: Int) = flow {
        val response = client.get(
            buildUrl { path(queueDownloadChapterRequest(mangaId, chapterIndex)) }
        ) {
            expectSuccess = true
        }
        emit(response)
    }.flowOn(Dispatchers.IO)

    fun queueChapterDownload(chapter: Chapter) = queueChapterDownload(chapter.mangaId, chapter.index)

    fun queueChapterDownload(manga: Manga, chapterIndex: Int) = queueChapterDownload(manga.id, chapterIndex)

    fun queueChapterDownload(manga: Manga, chapter: Chapter) = queueChapterDownload(manga.id, chapter.index)

    fun stopChapterDownload(mangaId: Long, chapterIndex: Int) = flow {
        val response = client.delete(
            buildUrl { path(stopDownloadingChapterRequest(mangaId, chapterIndex)) }
        ) {
            expectSuccess = true
        }
        emit(response)
    }.flowOn(Dispatchers.IO)

    fun stopChapterDownload(chapter: Chapter) = stopChapterDownload(chapter.mangaId, chapter.index)

    fun stopChapterDownload(manga: Manga, chapterIndex: Int) = stopChapterDownload(manga.id, chapterIndex)

    fun stopChapterDownload(manga: Manga, chapter: Chapter) = stopChapterDownload(manga.id, chapter.index)

    fun updateChapterMeta(mangaId: Long, chapterIndex: Int, key: String, value: String) = flow {
        val response = client.submitForm(
            buildUrl { path(updateChapterMetaRequest(mangaId, chapterIndex)) },
            formParameters = Parameters.build {
                append("key", key)
                append("value", value)
            }
        ) {
            method = HttpMethod.Patch
            expectSuccess = true
        }
        emit(response)
    }.flowOn(Dispatchers.IO)

    fun updateChapterMeta(chapter: Chapter, key: String, value: String) = updateChapterMeta(chapter.mangaId, chapter.index, key, value)

    fun updateChapterMeta(manga: Manga, chapterIndex: Int, key: String, value: String) = updateChapterMeta(manga.id, chapterIndex, key, value)

    fun updateChapterMeta(manga: Manga, chapter: Chapter, key: String, value: String) = updateChapterMeta(manga.id, chapter.index, key, value)
}
