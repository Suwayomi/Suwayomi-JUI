/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.data.server.interactions

import ca.gosyer.data.models.Extension
import ca.gosyer.data.server.Http
import ca.gosyer.data.server.ServerPreferences
import ca.gosyer.data.server.requests.apkIconQuery
import ca.gosyer.data.server.requests.apkInstallQuery
import ca.gosyer.data.server.requests.apkUninstallQuery
import ca.gosyer.data.server.requests.apkUpdateQuery
import ca.gosyer.data.server.requests.extensionListQuery
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.utils.io.ByteReadChannel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import me.tatarka.inject.annotations.Inject

class ExtensionInteractionHandler @Inject constructor(
    client: Http,
    serverPreferences: ServerPreferences
) : BaseInteractionHandler(client, serverPreferences) {

    fun getExtensionList() = flow {
        val response = client.get<List<Extension>>(
            serverUrl + extensionListQuery()
        )
        emit(response)
    }.flowOn(Dispatchers.IO)

    fun installExtension(extension: Extension) = flow {
        val response = client.get<HttpResponse>(
            serverUrl + apkInstallQuery(extension.pkgName)
        )
        emit(response)
    }.flowOn(Dispatchers.IO)

    fun updateExtension(extension: Extension) = flow {
        val response = client.get<HttpResponse>(
            serverUrl + apkUpdateQuery(extension.pkgName)
        )
        emit(response)
    }.flowOn(Dispatchers.IO)

    fun uninstallExtension(extension: Extension) = flow {
        val response = client.get<HttpResponse>(
            serverUrl + apkUninstallQuery(extension.pkgName)
        )
        emit(response)
    }.flowOn(Dispatchers.IO)

    fun getApkIcon(extension: Extension, block: HttpRequestBuilder.() -> Unit) = flow {
        val response = client.get<ByteReadChannel>(
            serverUrl + apkIconQuery(extension.apkName),
            block
        )
        emit(response)
    }.flowOn(Dispatchers.IO)
}
