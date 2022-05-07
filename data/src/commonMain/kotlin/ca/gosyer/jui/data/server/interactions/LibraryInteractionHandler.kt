/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.data.server.interactions

import ca.gosyer.jui.core.lang.IO
import ca.gosyer.jui.data.models.Manga
import ca.gosyer.jui.data.server.Http
import ca.gosyer.jui.data.server.ServerPreferences
import ca.gosyer.jui.data.server.requests.addMangaToLibraryQuery
import ca.gosyer.jui.data.server.requests.removeMangaFromLibraryRequest
import io.ktor.client.plugins.expectSuccess
import io.ktor.client.request.delete
import io.ktor.client.request.get
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import me.tatarka.inject.annotations.Inject

class LibraryInteractionHandler @Inject constructor(
    client: Http,
    serverPreferences: ServerPreferences
) : BaseInteractionHandler(client, serverPreferences) {

    fun addMangaToLibrary(mangaId: Long) = flow {
        val response = client.get(
            serverUrl + addMangaToLibraryQuery(mangaId)
        ) {
            expectSuccess = true
        }
        emit(response)
    }.flowOn(Dispatchers.IO)

    fun addMangaToLibrary(manga: Manga) = addMangaToLibrary(manga.id)

    fun removeMangaFromLibrary(mangaId: Long) = flow {
        val response = client.delete(
            serverUrl + removeMangaFromLibraryRequest(mangaId)
        ) {
            expectSuccess = true
        }
        emit(response)
    }.flowOn(Dispatchers.IO)

    fun removeMangaFromLibrary(manga: Manga) = removeMangaFromLibrary(manga.id)
}
