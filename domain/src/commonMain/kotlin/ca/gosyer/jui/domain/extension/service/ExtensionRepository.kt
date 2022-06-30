/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.domain.extension.service

import ca.gosyer.jui.domain.extension.model.Extension
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.statement.HttpResponse
import io.ktor.utils.io.ByteReadChannel
import kotlinx.coroutines.flow.Flow

interface ExtensionRepository {
    fun getExtensionList(): Flow<List<Extension>>
    fun installExtension(extension: Extension): Flow<HttpResponse>
    fun updateExtension(extension: Extension): Flow<HttpResponse>
    fun uninstallExtension(extension: Extension): Flow<HttpResponse>
    fun getApkIcon(extension: Extension, block: HttpRequestBuilder.() -> Unit): Flow<ByteReadChannel>
}
