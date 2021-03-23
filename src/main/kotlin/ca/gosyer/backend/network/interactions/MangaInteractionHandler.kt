/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.backend.network.interactions

import ca.gosyer.backend.models.Manga
import ca.gosyer.backend.network.requests.mangaQuery
import ca.gosyer.backend.network.requests.mangaThumbnailQuery
import io.ktor.client.HttpClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MangaInteractionHandler(private val client: HttpClient): BaseInteractionHandler() {

    suspend fun getManga(mangaId: Long) = withContext(Dispatchers.IO) {
        client.getRepeat<Manga>(
            serverUrl + mangaQuery(mangaId)
        )
    }

    suspend fun getMangaThumbnail(mangaId: Long) = withContext(Dispatchers.IO) {
        imageFromUrl(
            client,
            serverUrl + mangaThumbnailQuery(mangaId)
        )
    }

}