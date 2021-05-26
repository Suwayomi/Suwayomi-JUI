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
import ca.gosyer.util.lang.withIOContext
import io.ktor.client.request.parameter
import javax.inject.Inject

class MangaInteractionHandler @Inject constructor(
    client: Http,
    serverPreferences: ServerPreferences
) : BaseInteractionHandler(client, serverPreferences) {

    suspend fun getManga(mangaId: Long, refresh: Boolean = false) = withIOContext {
        client.getRepeat<Manga>(
            serverUrl + mangaQuery(mangaId)
        ) {
            url {
                if (refresh) {
                    parameter("onlineFetch", true)
                }
            }
        }
    }

    suspend fun getManga(manga: Manga, refresh: Boolean = false) = getManga(manga.id, refresh)

    suspend fun getMangaThumbnail(mangaId: Long) = withIOContext {
        imageFromUrl(
            client,
            serverUrl + mangaThumbnailQuery(mangaId)
        )
    }
}
