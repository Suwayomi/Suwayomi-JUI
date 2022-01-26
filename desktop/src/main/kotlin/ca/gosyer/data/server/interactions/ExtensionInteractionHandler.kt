/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.data.server.interactions

import ca.gosyer.core.lang.withIOContext
import ca.gosyer.data.models.Extension
import ca.gosyer.data.server.Http
import ca.gosyer.data.server.ServerPreferences
import ca.gosyer.data.server.requests.apkIconQuery
import ca.gosyer.data.server.requests.apkInstallQuery
import ca.gosyer.data.server.requests.apkUninstallQuery
import ca.gosyer.data.server.requests.apkUpdateQuery
import ca.gosyer.data.server.requests.extensionListQuery
import ca.gosyer.util.compose.imageFromUrl
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import javax.inject.Inject

class ExtensionInteractionHandler @Inject constructor(
    client: Http,
    serverPreferences: ServerPreferences
) : BaseInteractionHandler(client, serverPreferences) {

    suspend fun getExtensionList() = withIOContext {
        client.get<List<Extension>>(
            serverUrl + extensionListQuery()
        )
    }

    suspend fun installExtension(extension: Extension) = withIOContext {
        client.get<HttpResponse>(
            serverUrl + apkInstallQuery(extension.pkgName)
        )
    }

    suspend fun updateExtension(extension: Extension) = withIOContext {
        client.get<HttpResponse>(
            serverUrl + apkUpdateQuery(extension.pkgName)
        )
    }

    suspend fun uninstallExtension(extension: Extension) = withIOContext {
        client.get<HttpResponse>(
            serverUrl + apkUninstallQuery(extension.pkgName)
        )
    }

    suspend fun getApkIcon(extension: Extension, block: HttpRequestBuilder.() -> Unit) = withIOContext {
        imageFromUrl(
            client,
            serverUrl + apkIconQuery(extension.apkName),
            block
        )
    }
}
