/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.data.server.interactions

import ca.gosyer.data.server.Http
import ca.gosyer.data.server.ServerPreferences
import ca.gosyer.data.server.requests.downloadsClearRequest
import ca.gosyer.data.server.requests.downloadsStartRequest
import ca.gosyer.data.server.requests.downloadsStopRequest
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import me.tatarka.inject.annotations.Inject

class DownloadInteractionHandler @Inject constructor(
    client: Http,
    serverPreferences: ServerPreferences
) : BaseInteractionHandler(client, serverPreferences) {

    fun startDownloading() = flow {
        val response = client.get<HttpResponse>(
            serverUrl + downloadsStartRequest()
        )
        emit(response)
    }.flowOn(Dispatchers.IO)

    fun stopDownloading() = flow {
        val response = client.get<HttpResponse>(
            serverUrl + downloadsStopRequest()
        )
        emit(response)
    }.flowOn(Dispatchers.IO)

    fun clearDownloadQueue() = flow {
        val response = client.get<HttpResponse>(
            serverUrl + downloadsClearRequest()
        )
        emit(response)
    }.flowOn(Dispatchers.IO)
}
