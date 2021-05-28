/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.data.server.interactions

import ca.gosyer.data.models.Backup
import ca.gosyer.data.server.Http
import ca.gosyer.data.server.ServerPreferences
import ca.gosyer.data.server.requests.backupExportRequest
import ca.gosyer.data.server.requests.backupFileExportRequest
import ca.gosyer.data.server.requests.backupFileImportRequest
import ca.gosyer.data.server.requests.backupImportRequest
import ca.gosyer.util.lang.withIOContext
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import java.io.File
import javax.inject.Inject

class BackupInteractionHandler @Inject constructor(
    client: Http,
    serverPreferences: ServerPreferences
) : BaseInteractionHandler(client, serverPreferences) {

    suspend fun importBackupFile(file: File, block: HttpRequestBuilder.() -> Unit = {}) = withIOContext {
        client.submitFormWithBinaryData<HttpResponse>(
            serverUrl + backupFileImportRequest(),
            formData = formData {
                append(
                    "backup.json", file.readBytes(),
                    Headers.build {
                        append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                        append(HttpHeaders.ContentDisposition, "filename=backup.json")
                    }
                )
            },
            block = block
        )
    }

    suspend fun importBackup(backup: Backup, block: HttpRequestBuilder.() -> Unit = {}) = withIOContext {
        client.postRepeat<HttpResponse>(
            serverUrl + backupImportRequest()
        ) {
            contentType(ContentType.Application.Json)
            body = backup
            block()
        }
    }

    suspend fun exportBackupFile(block: HttpRequestBuilder.() -> Unit = {}) = withIOContext {
        client.getRepeat<HttpResponse>(
            serverUrl + backupFileExportRequest(),
            block
        )
    }

    suspend fun exportBackup(block: HttpRequestBuilder.() -> Unit = {}) = withIOContext {
        client.getRepeat<Backup>(
            serverUrl + backupExportRequest(),
            block
        )
    }
}
