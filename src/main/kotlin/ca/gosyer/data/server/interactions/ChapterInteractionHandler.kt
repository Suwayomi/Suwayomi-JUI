/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.data.server.interactions

import ca.gosyer.data.models.Chapter
import ca.gosyer.data.models.Manga
import ca.gosyer.data.server.Http
import ca.gosyer.data.server.ServerPreferences
import ca.gosyer.data.server.requests.getChapterQuery
import ca.gosyer.data.server.requests.getMangaChaptersQuery
import ca.gosyer.data.server.requests.getPageQuery
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ChapterInteractionHandler @Inject constructor(
    client: Http,
    serverPreferences: ServerPreferences
): BaseInteractionHandler(client, serverPreferences) {

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