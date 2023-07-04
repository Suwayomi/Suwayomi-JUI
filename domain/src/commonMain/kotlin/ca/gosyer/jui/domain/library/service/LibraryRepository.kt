/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.domain.library.service

import de.jensklingenberg.ktorfit.http.DELETE
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Path
import io.ktor.client.statement.HttpResponse
import kotlinx.coroutines.flow.Flow

interface LibraryRepository {
    @GET("api/v1/manga/{mangaId}/library")
    fun addMangaToLibrary(
        @Path("mangaId") mangaId: Long,
    ): Flow<HttpResponse>

    @DELETE("api/v1/manga/{mangaId}/library")
    fun removeMangaFromLibrary(
        @Path("mangaId") mangaId: Long,
    ): Flow<HttpResponse>
}
