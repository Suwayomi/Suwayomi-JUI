/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.domain.extension.service

import ca.gosyer.jui.core.io.SYSTEM
import ca.gosyer.jui.domain.extension.model.Extension
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Multipart
import de.jensklingenberg.ktorfit.http.POST
import de.jensklingenberg.ktorfit.http.Part
import de.jensklingenberg.ktorfit.http.Path
import de.jensklingenberg.ktorfit.http.ReqBuilder
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.forms.formData
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.content.PartData
import io.ktor.utils.io.ByteReadChannel
import kotlinx.coroutines.flow.Flow
import okio.FileSystem
import okio.buffer

interface ExtensionRepository {
    @GET("api/v1/extension/list")
    fun getExtensionList(): Flow<List<Extension>>

    @Multipart
    @POST("api/v1/extension/install")
    fun installExtension(
        @Part("") formData: List<PartData>
    ): Flow<HttpResponse>

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

    companion object {
        fun buildExtensionFormData(file: okio.Path) = formData {
            append(
                "file",
                FileSystem.SYSTEM.source(file).buffer().readByteArray(),
                Headers.build {
                    append(HttpHeaders.ContentType, ContentType.MultiPart.FormData.toString())
                    append(HttpHeaders.ContentDisposition, "filename=file")
                }
            )
        }
    }
}
