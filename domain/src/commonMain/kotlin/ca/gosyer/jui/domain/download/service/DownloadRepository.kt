/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.domain.download.service

import de.jensklingenberg.ktorfit.http.GET
import io.ktor.client.statement.HttpResponse
import kotlinx.coroutines.flow.Flow

interface DownloadRepository {
    @GET("api/v1/downloads/start")
    fun startDownloading(): Flow<HttpResponse>

    @GET("api/v1/downloads/stop")
    fun stopDownloading(): Flow<HttpResponse>

    @GET("api/v1/downloads/clear")
    fun clearDownloadQueue(): Flow<HttpResponse>
}
