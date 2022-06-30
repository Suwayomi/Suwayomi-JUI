/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.domain.backup.service

import ca.gosyer.jui.domain.backup.model.BackupValidationResult
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.statement.HttpResponse
import kotlinx.coroutines.flow.Flow
import okio.Path

interface BackupRepository {
    fun importBackupFile(file: Path, block: HttpRequestBuilder.() -> Unit = {}): Flow<HttpResponse>
    fun validateBackupFile(file: Path, block: HttpRequestBuilder.() -> Unit = {}): Flow<BackupValidationResult>
    fun exportBackupFile(block: HttpRequestBuilder.() -> Unit = {}): Flow<HttpResponse>
}
