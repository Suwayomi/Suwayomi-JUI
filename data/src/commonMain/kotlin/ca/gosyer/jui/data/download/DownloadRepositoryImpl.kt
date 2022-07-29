/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.data.download

import ca.gosyer.jui.core.lang.IO
import ca.gosyer.jui.data.base.BaseRepository
import ca.gosyer.jui.domain.download.service.DownloadRepository
import ca.gosyer.jui.domain.server.Http
import ca.gosyer.jui.domain.server.model.requests.downloadsClearRequest
import ca.gosyer.jui.domain.server.model.requests.downloadsStartRequest
import ca.gosyer.jui.domain.server.model.requests.downloadsStopRequest
import ca.gosyer.jui.domain.server.service.ServerPreferences
import io.ktor.client.plugins.expectSuccess
import io.ktor.client.request.get
import io.ktor.http.path
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import me.tatarka.inject.annotations.Inject

class DownloadRepositoryImpl @Inject constructor(
    client: Http,
    serverPreferences: ServerPreferences
) : BaseRepository(client, serverPreferences), DownloadRepository {

    override fun startDownloading() = flow {
        val response = client.get(
            buildUrl { path(downloadsStartRequest()) }
        ) {
            expectSuccess = true
        }
        emit(response)
    }.flowOn(Dispatchers.IO)

    override fun stopDownloading() = flow {
        val response = client.get(
            buildUrl { path(downloadsStopRequest()) }
        ) {
            expectSuccess = true
        }
        emit(response)
    }.flowOn(Dispatchers.IO)

    override fun clearDownloadQueue() = flow {
        val response = client.get(
            buildUrl { path(downloadsClearRequest()) }
        ) {
            expectSuccess = true
        }
        emit(response)
    }.flowOn(Dispatchers.IO)
}
