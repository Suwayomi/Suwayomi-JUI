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
import ca.gosyer.data.server.requests.extensionListQuery
import io.ktor.client.statement.HttpResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ExtensionInteractionHandler @Inject constructor(
    client: Http,
    serverPreferences: ServerPreferences
): BaseInteractionHandler(client, serverPreferences) {

    suspend fun getExtensionList() = withContext(Dispatchers.IO) {
        client.getRepeat<List<Extension>>(
            serverUrl + extensionListQuery()
        )
    }

    suspend fun installExtension(extension: Extension) = withContext(Dispatchers.IO) {
        client.getRepeat<HttpResponse>(
            serverUrl + apkInstallQuery(extension.apkName)
        )
    }

    suspend fun uninstallExtension(extension: Extension) = withContext(Dispatchers.IO) {
        client.getRepeat<HttpResponse>(
            serverUrl + apkUninstallQuery(extension.apkName)
        )
    }

    suspend fun getApkIcon(extension: Extension) = withContext(Dispatchers.IO) {
        imageFromUrl(
            client,
            serverUrl + apkIconQuery(extension.apkName)
        )
    }
}