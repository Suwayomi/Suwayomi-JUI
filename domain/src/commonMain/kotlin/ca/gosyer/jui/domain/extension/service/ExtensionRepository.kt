/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.domain.extension.service

import ca.gosyer.jui.domain.extension.model.Extension
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Path
import de.jensklingenberg.ktorfit.http.ReqBuilder
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.statement.HttpResponse
import io.ktor.utils.io.ByteReadChannel
import kotlinx.coroutines.flow.Flow

interface ExtensionRepository {
    @GET("api/v1/extension/list")
    fun getExtensionList(): Flow<List<Extension>>

    @GET("api/v1/extension/install/{pkgName}")
    fun installExtension(
        @Path("pkgName") pkgName: String
    ): Flow<HttpResponse>

    @GET("api/v1/extension/update/{pkgName}")
    fun updateExtension(
        @Path("pkgName") pkgName: String
    ): Flow<HttpResponse>

    @GET("api/v1/extension/uninstall/{pkgName}")
    fun uninstallExtension(
        @Path("pkgName") pkgName: String
    ): Flow<HttpResponse>

    @GET("api/v1/extension/icon/{apkName}")
    fun getApkIcon(
        @Path("apkName") apkName: String,
        @ReqBuilder block: HttpRequestBuilder.() -> Unit
    ): Flow<ByteReadChannel>
}
