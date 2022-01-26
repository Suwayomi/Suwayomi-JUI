/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.data.server.interactions

import ca.gosyer.data.models.BackupValidationResult
import ca.gosyer.data.server.Http
import ca.gosyer.data.server.ServerPreferences
import ca.gosyer.data.server.requests.backupFileExportRequest
import ca.gosyer.data.server.requests.backupFileImportRequest
import ca.gosyer.data.server.requests.validateBackupFileRequest
import ca.gosyer.util.lang.withIOContext
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import java.nio.file.Path
import javax.inject.Inject
import kotlin.io.path.readBytes

class BackupInteractionHandler @Inject constructor(
    client: Http,
    serverPreferences: ServerPreferences
) : BaseInteractionHandler(client, serverPreferences) {

    suspend fun importBackupFile(file: Path, block: HttpRequestBuilder.() -> Unit = {}) = withIOContext {
        client.submitFormWithBinaryData<HttpResponse>(
            serverUrl + backupFileImportRequest(),
            formData = formData {
                append(
                    "backup.proto.gz", file.readBytes(),
                    Headers.build {
                        append(HttpHeaders.ContentType, ContentType.MultiPart.FormData.toString())
                        append(HttpHeaders.ContentDisposition, "filename=backup.proto.gz")
                    }
                )
            },
            block = block
        )
    }

    suspend fun validateBackupFile(file: Path, block: HttpRequestBuilder.() -> Unit = {}) = withIOContext {
        client.submitFormWithBinaryData<BackupValidationResult>(
            serverUrl + validateBackupFileRequest(),
            formData = formData {
                append(
                    "backup.proto.gz", file.readBytes(),
                    Headers.build {
                        append(HttpHeaders.ContentType, ContentType.MultiPart.FormData.toString())
                        append(HttpHeaders.ContentDisposition, "filename=backup.proto.gz")
                    }
                )
            },
            block = block
        )
    }

    suspend fun exportBackupFile(block: HttpRequestBuilder.() -> Unit = {}) = withIOContext {
        client.get<HttpResponse>(
            serverUrl + backupFileExportRequest(),
            block
        )
    }
}
