/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.data.library

import ca.gosyer.jui.core.lang.IO
import ca.gosyer.jui.data.base.BaseRepository
import ca.gosyer.jui.domain.library.service.LibraryRepository
import ca.gosyer.jui.domain.server.Http
import ca.gosyer.jui.domain.server.model.requests.addMangaToLibraryQuery
import ca.gosyer.jui.domain.server.model.requests.removeMangaFromLibraryRequest
import ca.gosyer.jui.domain.server.service.ServerPreferences
import io.ktor.client.plugins.expectSuccess
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.http.path
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import me.tatarka.inject.annotations.Inject

class LibraryRepositoryImpl @Inject constructor(
    client: Http,
    serverPreferences: ServerPreferences
) : BaseRepository(client, serverPreferences), LibraryRepository {

    override fun addMangaToLibrary(mangaId: Long) = flow {
        val response = client.get(
            buildUrl { path(addMangaToLibraryQuery(mangaId)) },
        ) {
            expectSuccess = true
        }
        emit(response)
    }.flowOn(Dispatchers.IO)

    override fun removeMangaFromLibrary(mangaId: Long) = flow {
        val response = client.delete(
            buildUrl { path(removeMangaFromLibraryRequest(mangaId)) },
        ) {
            expectSuccess = true
        }
        emit(response)
    }.flowOn(Dispatchers.IO)
}
