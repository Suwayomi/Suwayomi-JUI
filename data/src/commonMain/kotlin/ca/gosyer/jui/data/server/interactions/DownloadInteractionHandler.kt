/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.data.server.interactions

import ca.gosyer.jui.core.io.asSuccess
import ca.gosyer.jui.core.lang.IO
import ca.gosyer.jui.data.server.Http
import ca.gosyer.jui.data.server.ServerPreferences
import ca.gosyer.jui.data.server.requests.downloadsClearRequest
import ca.gosyer.jui.data.server.requests.downloadsStartRequest
import ca.gosyer.jui.data.server.requests.downloadsStopRequest
import io.ktor.client.request.get
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import me.tatarka.inject.annotations.Inject

class DownloadInteractionHandler @Inject constructor(
    client: Http,
    serverPreferences: ServerPreferences
) : BaseInteractionHandler(client, serverPreferences) {

    fun startDownloading() = flow {
        val response = client.get(
            serverUrl + downloadsStartRequest()
        ).asSuccess()
        emit(response)
    }.flowOn(Dispatchers.IO)

    fun stopDownloading() = flow {
        val response = client.get(
            serverUrl + downloadsStopRequest()
        ).asSuccess()
        emit(response)
    }.flowOn(Dispatchers.IO)

    fun clearDownloadQueue() = flow {
        val response = client.get(
            serverUrl + downloadsClearRequest()
        ).asSuccess()
        emit(response)
    }.flowOn(Dispatchers.IO)
}
