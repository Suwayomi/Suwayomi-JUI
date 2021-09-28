/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.data.server.interactions

import ca.gosyer.data.models.MangaAndChapter
import ca.gosyer.data.server.Http
import ca.gosyer.data.server.ServerPreferences
import ca.gosyer.data.server.requests.recentUpdatesQuery
import ca.gosyer.util.lang.withIOContext
import io.ktor.client.request.get
import javax.inject.Inject

class UpdatesInteractionHandler @Inject constructor(
    client: Http,
    serverPreferences: ServerPreferences
) : BaseInteractionHandler(client, serverPreferences) {

    suspend fun getRecentUpdates() = withIOContext {
        client.get<List<MangaAndChapter>>(
            serverUrl + recentUpdatesQuery()
        )
    }
}
