/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.data.server.interactions

import ca.gosyer.jui.core.lang.IO
import ca.gosyer.jui.data.models.Extension
import ca.gosyer.jui.data.server.Http
import ca.gosyer.jui.data.server.ServerPreferences
import ca.gosyer.jui.data.server.requests.apkIconQuery
import ca.gosyer.jui.data.server.requests.apkInstallQuery
import ca.gosyer.jui.data.server.requests.apkUninstallQuery
import ca.gosyer.jui.data.server.requests.apkUpdateQuery
import ca.gosyer.jui.data.server.requests.extensionListQuery
import io.ktor.client.call.body
import io.ktor.client.plugins.expectSuccess
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsChannel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import me.tatarka.inject.annotations.Inject

class ExtensionInteractionHandler @Inject constructor(
    client: Http,
    serverPreferences: ServerPreferences
) : BaseInteractionHandler(client, serverPreferences) {

    fun getExtensionList() = flow {
        val response = client.get(
            serverUrl + extensionListQuery()
        ) {
            expectSuccess = true
        }.body<List<Extension>>()
        emit(response)
    }.flowOn(Dispatchers.IO)

    fun installExtension(extension: Extension) = flow {
        val response = client.get(
            serverUrl + apkInstallQuery(extension.pkgName)
        ) {
            expectSuccess = true
        }
        emit(response)
    }.flowOn(Dispatchers.IO)

    fun updateExtension(extension: Extension) = flow {
        val response = client.get(
            serverUrl + apkUpdateQuery(extension.pkgName)
        ) {
            expectSuccess = true
        }
        emit(response)
    }.flowOn(Dispatchers.IO)

    fun uninstallExtension(extension: Extension) = flow {
        val response = client.get(
            serverUrl + apkUninstallQuery(extension.pkgName)
        ) {
            expectSuccess = true
        }
        emit(response)
    }.flowOn(Dispatchers.IO)

    fun getApkIcon(extension: Extension, block: HttpRequestBuilder.() -> Unit) = flow {
        val response = client.get(
            serverUrl + apkIconQuery(extension.apkName)
        ) {
            expectSuccess = true
            block()
        }.bodyAsChannel()
        emit(response)
    }.flowOn(Dispatchers.IO)
}
