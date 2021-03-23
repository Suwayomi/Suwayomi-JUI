/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.backend.network.interactions

import ca.gosyer.backend.models.Extension
import ca.gosyer.backend.network.requests.apkIconQuery
import ca.gosyer.backend.network.requests.apkInstallQuery
import ca.gosyer.backend.network.requests.apkUninstallQuery
import ca.gosyer.backend.network.requests.extensionListQuery
import io.ktor.client.HttpClient
import io.ktor.client.statement.HttpResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ExtensionInteractionHandler(private val client: HttpClient): BaseInteractionHandler() {

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