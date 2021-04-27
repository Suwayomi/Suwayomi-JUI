/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.data.server.interactions

import ca.gosyer.data.models.Manga
import ca.gosyer.data.server.Http
import ca.gosyer.data.server.ServerPreferences
import ca.gosyer.data.server.requests.addMangaToLibraryQuery
import ca.gosyer.data.server.requests.getLibraryQuery
import ca.gosyer.data.server.requests.removeMangaFromLibraryRequest
import io.ktor.client.statement.HttpResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class LibraryInteractionHandler @Inject constructor(
    client: Http,
    serverPreferences: ServerPreferences
) : BaseInteractionHandler(client, serverPreferences) {

    suspend fun getLibraryManga() = withContext(Dispatchers.IO) {
        client.getRepeat<List<Manga>>(
            serverUrl + getLibraryQuery()
        )
    }

    suspend fun addMangaToLibrary(mangaId: Long) = withContext(Dispatchers.IO) {
        client.getRepeat<HttpResponse>(
            serverUrl + addMangaToLibraryQuery(mangaId)
        )
    }

    suspend fun addMangaToLibrary(manga: Manga) = addMangaToLibrary(manga.id)

    suspend fun removeMangaFromLibrary(mangaId: Long) = withContext(Dispatchers.IO) {
        client.deleteRepeat<HttpResponse>(
            serverUrl + removeMangaFromLibraryRequest(mangaId)
        )
    }

    suspend fun removeMangaFromLibrary(manga: Manga) = removeMangaFromLibrary(manga.id)
}
