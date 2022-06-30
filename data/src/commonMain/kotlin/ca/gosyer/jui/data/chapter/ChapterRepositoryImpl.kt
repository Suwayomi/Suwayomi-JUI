/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.data.chapter

import ca.gosyer.jui.core.lang.IO
import ca.gosyer.jui.data.base.BaseRepository
import ca.gosyer.jui.domain.chapter.model.Chapter
import ca.gosyer.jui.domain.chapter.service.ChapterRepository
import ca.gosyer.jui.domain.server.Http
import ca.gosyer.jui.domain.server.model.requests.deleteDownloadedChapterRequest
import ca.gosyer.jui.domain.server.model.requests.getChapterQuery
import ca.gosyer.jui.domain.server.model.requests.getMangaChaptersQuery
import ca.gosyer.jui.domain.server.model.requests.getPageQuery
import ca.gosyer.jui.domain.server.model.requests.queueDownloadChapterRequest
import ca.gosyer.jui.domain.server.model.requests.stopDownloadingChapterRequest
import ca.gosyer.jui.domain.server.model.requests.updateChapterMetaRequest
import ca.gosyer.jui.domain.server.model.requests.updateChapterRequest
import ca.gosyer.jui.domain.server.service.ServerPreferences
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

class ChapterRepositoryImpl @Inject constructor(
    client: Http,
    serverPreferences: ServerPreferences
) : BaseRepository(client, serverPreferences), ChapterRepository {

    override fun getChapters(mangaId: Long, refresh: Boolean) = flow {
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

    override fun getChapter(mangaId: Long, chapterIndex: Int) = flow {
        val response = client.get(
            buildUrl { path(getChapterQuery(mangaId, chapterIndex)) }
        ) {
            expectSuccess = true
        }.body<Chapter>()
        emit(response)
    }.flowOn(Dispatchers.IO)

    override fun updateChapter(
        mangaId: Long,
        chapterIndex: Int,
        read: Boolean?,
        bookmarked: Boolean?,
        lastPageRead: Int?,
        markPreviousRead: Boolean?
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

    override fun getPage(mangaId: Long, chapterIndex: Int, pageNum: Int, block: HttpRequestBuilder.() -> Unit) = flow {
        val response = client.get(
            buildUrl { path(getPageQuery(mangaId, chapterIndex, pageNum)) }
        ) {
            expectSuccess = true
            block()
        }
        emit(response)
    }.flowOn(Dispatchers.IO)

    override fun deleteChapterDownload(mangaId: Long, chapterIndex: Int) = flow {
        val response = client.delete(
            buildUrl { path(deleteDownloadedChapterRequest(mangaId, chapterIndex)) }
        ) {
            expectSuccess = true
        }
        emit(response)
    }.flowOn(Dispatchers.IO)

    override fun queueChapterDownload(mangaId: Long, chapterIndex: Int) = flow {
        val response = client.get(
            buildUrl { path(queueDownloadChapterRequest(mangaId, chapterIndex)) }
        ) {
            expectSuccess = true
        }
        emit(response)
    }.flowOn(Dispatchers.IO)

    override fun stopChapterDownload(mangaId: Long, chapterIndex: Int) = flow {
        val response = client.delete(
            buildUrl { path(stopDownloadingChapterRequest(mangaId, chapterIndex)) }
        ) {
            expectSuccess = true
        }
        emit(response)
    }.flowOn(Dispatchers.IO)

    override fun updateChapterMeta(mangaId: Long, chapterIndex: Int, key: String, value: String) = flow {
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
}
