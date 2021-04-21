/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.data.server.interactions

import ca.gosyer.data.models.Manga
import ca.gosyer.data.server.Http
import ca.gosyer.data.server.ServerPreferences
import ca.gosyer.data.server.requests.mangaQuery
import ca.gosyer.data.server.requests.mangaThumbnailQuery
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class MangaInteractionHandler @Inject constructor(
    client: Http,
    serverPreferences: ServerPreferences
): BaseInteractionHandler(client, serverPreferences) {

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