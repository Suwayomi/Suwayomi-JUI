/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.data.server.interactions

import ca.gosyer.core.io.SYSTEM
import ca.gosyer.core.lang.IO
import ca.gosyer.data.models.BackupValidationResult
import ca.gosyer.data.server.Http
import ca.gosyer.data.server.ServerPreferences
import ca.gosyer.data.server.requests.backupFileExportRequest
import ca.gosyer.data.server.requests.backupFileImportRequest
import ca.gosyer.data.server.requests.validateBackupFileRequest
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
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

    fun importBackupFile(file: Path, block: HttpRequestBuilder.() -> Unit = {}) = flow {
        val response = client.submitFormWithBinaryData<HttpResponse>(
            serverUrl + backupFileImportRequest(),
            formData = formData {
                append(
                    "backup.proto.gz", FileSystem.SYSTEM.source(file).buffer().readByteArray(),
                    Headers.build {
                        append(HttpHeaders.ContentType, ContentType.MultiPart.FormData.toString())
                        append(HttpHeaders.ContentDisposition, "filename=backup.proto.gz")
                    }
                )
            },
            block = block
        )
        emit(response)
    }.flowOn(Dispatchers.IO)

    fun validateBackupFile(file: Path, block: HttpRequestBuilder.() -> Unit = {}) = flow {
        val response = client.submitFormWithBinaryData<BackupValidationResult>(
            serverUrl + validateBackupFileRequest(),
            formData = formData {
                append(
                    "backup.proto.gz", FileSystem.SYSTEM.source(file).buffer().readByteArray(),
                    Headers.build {
                        append(HttpHeaders.ContentType, ContentType.MultiPart.FormData.toString())
                        append(HttpHeaders.ContentDisposition, "filename=backup.proto.gz")
                    }
                )
            },
            block = block
        )
        emit(response)
    }.flowOn(Dispatchers.IO)

    fun exportBackupFile(block: HttpRequestBuilder.() -> Unit = {}) = flow {
        val response = client.get<HttpResponse>(
            serverUrl + backupFileExportRequest(),
            block
        )
        emit(response)
    }.flowOn(Dispatchers.IO)
}
