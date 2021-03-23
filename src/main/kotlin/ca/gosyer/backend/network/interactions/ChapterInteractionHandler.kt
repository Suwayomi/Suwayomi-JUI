/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.backend.network.interactions

import ca.gosyer.backend.models.Chapter
import ca.gosyer.backend.models.Manga
import ca.gosyer.backend.network.requests.getChapterQuery
import ca.gosyer.backend.network.requests.getMangaChaptersQuery
import ca.gosyer.backend.network.requests.getPageQuery
import io.ktor.client.HttpClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ChapterInteractionHandler(private val client: HttpClient): BaseInteractionHandler() {

    suspend fun getChapters(mangaId: Long) = withContext(Dispatchers.IO) {
        client.getRepeat<List<Chapter>>(
            serverUrl + getMangaChaptersQuery(mangaId)
        )
    }

    suspend fun getChapters(manga: Manga) = withContext(Dispatchers.IO) {
        client.getRepeat<Chapter>(
            serverUrl + getMangaChaptersQuery(manga.id)
        )
    }

    suspend fun getChapter(mangaId: Long, chapterId: Long) = withContext(Dispatchers.IO) {
        client.getRepeat<Chapter>(
            serverUrl + getChapterQuery(mangaId, chapterId)
        )
    }

    suspend fun getChapter(chapter: Chapter) = getChapter(chapter.mangaId, chapter.id)

    suspend fun getChapter(manga: Manga, chapterId: Long) = getChapter(manga.id, chapterId)

    suspend fun getChapter(manga: Manga, chapter: Chapter) = getChapter(manga.id, chapter.id)

    suspend fun getPage(mangaId: Long, chapterId: Long, pageNum: Int) = withContext(Dispatchers.IO) {
        imageFromUrl(
            client,
            serverUrl + getPageQuery(mangaId, chapterId, pageNum)
        )
    }

    suspend fun getPage(chapter: Chapter, pageNum: Int) = getPage(chapter.mangaId, chapter.id, pageNum)

    suspend fun getPage(manga: Manga, chapterId: Long, pageNum: Int) = getPage(manga.id, chapterId, pageNum)

    suspend fun getPage(manga: Manga, chapter: Chapter, pageNum: Int) = getPage(manga.id, chapter.id, pageNum)
}