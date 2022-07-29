/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.data.extension

import ca.gosyer.jui.core.lang.IO
import ca.gosyer.jui.data.base.BaseRepository
import ca.gosyer.jui.domain.extension.model.Extension
import ca.gosyer.jui.domain.extension.service.ExtensionRepository
import ca.gosyer.jui.domain.server.Http
import ca.gosyer.jui.domain.server.model.requests.apkIconQuery
import ca.gosyer.jui.domain.server.model.requests.apkInstallQuery
import ca.gosyer.jui.domain.server.model.requests.apkUninstallQuery
import ca.gosyer.jui.domain.server.model.requests.apkUpdateQuery
import ca.gosyer.jui.domain.server.model.requests.extensionListQuery
import ca.gosyer.jui.domain.server.service.ServerPreferences
import io.ktor.client.call.body
import io.ktor.client.plugins.expectSuccess
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.path
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import me.tatarka.inject.annotations.Inject

class ExtensionRepositoryImpl @Inject constructor(
    client: Http,
    serverPreferences: ServerPreferences
) : BaseRepository(client, serverPreferences), ExtensionRepository {

    override fun getExtensionList() = flow {
        val response = client.get(
            buildUrl { path(extensionListQuery()) }
        ) {
            expectSuccess = true
        }.body<List<Extension>>()
        emit(response)
    }.flowOn(Dispatchers.IO)

    override fun installExtension(extension: Extension) = flow {
        val response = client.get(
            buildUrl { path(apkInstallQuery(extension.pkgName)) }
        ) {
            expectSuccess = true
        }
        emit(response)
    }.flowOn(Dispatchers.IO)

    override fun updateExtension(extension: Extension) = flow {
        val response = client.get(
            buildUrl { path(apkUpdateQuery(extension.pkgName)) }
        ) {
            expectSuccess = true
        }
        emit(response)
    }.flowOn(Dispatchers.IO)

    override fun uninstallExtension(extension: Extension) = flow {
        val response = client.get(
            buildUrl { path(apkUninstallQuery(extension.pkgName)) }
        ) {
            expectSuccess = true
        }
        emit(response)
    }.flowOn(Dispatchers.IO)

    override fun getApkIcon(extension: Extension, block: HttpRequestBuilder.() -> Unit) = flow {
        val response = client.get(
            buildUrl { path(apkIconQuery(extension.apkName)) }
        ) {
            expectSuccess = true
            block()
        }.bodyAsChannel()
        emit(response)
    }.flowOn(Dispatchers.IO)
}
