/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.domain.library.service

import io.ktor.client.statement.HttpResponse
import kotlinx.coroutines.flow.Flow

interface LibraryRepository {
    fun addMangaToLibrary(mangaId: Long): Flow<HttpResponse>
    fun removeMangaFromLibrary(mangaId: Long): Flow<HttpResponse>
}
