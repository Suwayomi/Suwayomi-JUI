/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.domain.chapter.service

import ca.gosyer.jui.domain.chapter.model.Chapter
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.statement.HttpResponse
import kotlinx.coroutines.flow.Flow

interface ChapterRepository {
    fun getChapters(mangaId: Long, refresh: Boolean = false): Flow<List<Chapter>>
    fun getChapter(mangaId: Long, chapterIndex: Int): Flow<Chapter>
    fun updateChapter(
        mangaId: Long,
        chapterIndex: Int,
        read: Boolean? = null,
        bookmarked: Boolean? = null,
        lastPageRead: Int? = null,
        markPreviousRead: Boolean? = null
    ): Flow<HttpResponse>

    fun getPage(
        mangaId: Long,
        chapterIndex: Int,
        pageNum: Int,
        block: HttpRequestBuilder.() -> Unit
    ): Flow<HttpResponse>

    fun deleteChapterDownload(mangaId: Long, chapterIndex: Int): Flow<HttpResponse>
    fun queueChapterDownload(mangaId: Long, chapterIndex: Int): Flow<HttpResponse>
    fun stopChapterDownload(mangaId: Long, chapterIndex: Int): Flow<HttpResponse>
    fun updateChapterMeta(mangaId: Long, chapterIndex: Int, key: String, value: String): Flow<HttpResponse>
}
