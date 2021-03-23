/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.backend.network.interactions

import ca.gosyer.backend.models.Manga
import ca.gosyer.backend.network.requests.addMangaToLibraryQuery
import ca.gosyer.backend.network.requests.getLibraryQuery
import ca.gosyer.backend.network.requests.removeMangaFromLibraryRequest
import io.ktor.client.HttpClient
import io.ktor.client.statement.HttpResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LibraryInteractionHandler(private val client: HttpClient): BaseInteractionHandler() {

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