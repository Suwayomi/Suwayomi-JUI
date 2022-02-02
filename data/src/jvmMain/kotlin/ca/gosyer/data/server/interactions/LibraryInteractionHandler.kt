/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.data.server.interactions

import ca.gosyer.core.lang.withIOContext
import ca.gosyer.data.models.Manga
import ca.gosyer.data.server.Http
import ca.gosyer.data.server.ServerPreferences
import ca.gosyer.data.server.requests.addMangaToLibraryQuery
import ca.gosyer.data.server.requests.removeMangaFromLibraryRequest
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import me.tatarka.inject.annotations.Inject

class LibraryInteractionHandler @Inject constructor(
    client: Http,
    serverPreferences: ServerPreferences
) : BaseInteractionHandler(client, serverPreferences) {

    suspend fun addMangaToLibrary(mangaId: Long) = withIOContext {
        client.get<HttpResponse>(
            serverUrl + addMangaToLibraryQuery(mangaId)
        )
    }

    suspend fun addMangaToLibrary(manga: Manga) = addMangaToLibrary(manga.id)

    suspend fun removeMangaFromLibrary(mangaId: Long) = withIOContext {
        client.delete<HttpResponse>(
            serverUrl + removeMangaFromLibraryRequest(mangaId)
        )
    }

    suspend fun removeMangaFromLibrary(manga: Manga) = removeMangaFromLibrary(manga.id)
}
