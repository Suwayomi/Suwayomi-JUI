/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.data.server.interactions

import ca.gosyer.core.lang.withIOContext
import ca.gosyer.data.server.Http
import ca.gosyer.data.server.ServerPreferences
import ca.gosyer.data.server.requests.downloadsClearRequest
import ca.gosyer.data.server.requests.downloadsStartRequest
import ca.gosyer.data.server.requests.downloadsStopRequest
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import me.tatarka.inject.annotations.Inject

class DownloadInteractionHandler @Inject constructor(
    client: Http,
    serverPreferences: ServerPreferences
) : BaseInteractionHandler(client, serverPreferences) {

    suspend fun startDownloading() = withIOContext {
        client.get<HttpResponse>(
            serverUrl + downloadsStartRequest()
        )
    }

    suspend fun stopDownloading() = withIOContext {
        client.get<HttpResponse>(
            serverUrl + downloadsStopRequest()
        )
    }

    suspend fun clearDownloadQueue() = withIOContext {
        client.get<HttpResponse>(
            serverUrl + downloadsClearRequest()
        )
    }
}
