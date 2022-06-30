/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.data.server.interactions

import ca.gosyer.jui.core.io.SYSTEM
import ca.gosyer.jui.core.lang.IO
import ca.gosyer.jui.data.models.BackupValidationResult
import ca.gosyer.jui.data.server.Http
import ca.gosyer.jui.data.server.ServerPreferences
import ca.gosyer.jui.data.server.requests.backupFileExportRequest
import ca.gosyer.jui.data.server.requests.backupFileImportRequest
import ca.gosyer.jui.data.server.requests.validateBackupFileRequest
import io.ktor.client.call.body
import io.ktor.client.plugins.expectSuccess
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.get
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.path
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import me.tatarka.inject.annotations.Inject
import okio.FileSystem
import okio.Path
import okio.buffer

class BackupInteractionHandler @Inject constructor(
    client: Http,
    serverPreferences: ServerPreferences
) : BaseInteractionHandler(client, serverPreferences) {

    private fun buildFormData(file: Path) = formData {
        append(
            "backup.proto.gz", FileSystem.SYSTEM.source(file).buffer().readByteArray(),
            Headers.build {
                append(HttpHeaders.ContentType, ContentType.MultiPart.FormData.toString())
                append(HttpHeaders.ContentDisposition, "filename=backup.proto.gz")
            }
        )
    }

    fun importBackupFile(file: Path, block: HttpRequestBuilder.() -> Unit = {}) = flow {
        val response = client.submitFormWithBinaryData(
            buildUrl { path(backupFileImportRequest()) },
            formData = buildFormData(file)
        ) {
            expectSuccess = true
            block()
        }
        emit(response)
    }.flowOn(Dispatchers.IO)

    fun validateBackupFile(file: Path, block: HttpRequestBuilder.() -> Unit = {}) = flow {
        val response = client.submitFormWithBinaryData(
            buildUrl { path(validateBackupFileRequest()) },
            formData = buildFormData(file)
        ) {
            expectSuccess = true
            block()
        }.body<BackupValidationResult>()
        emit(response)
    }.flowOn(Dispatchers.IO)

    fun exportBackupFile(block: HttpRequestBuilder.() -> Unit = {}) = flow {
        val response = client.get(
            buildUrl { path(backupFileExportRequest()) },
        ) {
            expectSuccess = true
            block()
        }
        emit(response)
    }.flowOn(Dispatchers.IO)
}
