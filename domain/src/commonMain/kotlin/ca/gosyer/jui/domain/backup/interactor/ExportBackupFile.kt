/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.domain.backup.interactor

import ca.gosyer.jui.domain.backup.service.BackupRepository
import io.ktor.client.request.HttpRequestBuilder
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.singleOrNull
import me.tatarka.inject.annotations.Inject
import org.lighthousegames.logging.logging

class ExportBackupFile
    @Inject
    constructor(private val backupRepository: BackupRepository) {
        suspend fun await(
            block: HttpRequestBuilder.() -> Unit = {},
            onError: suspend (Throwable) -> Unit = {},
        ) = asFlow(block)
            .catch {
                onError(it)
                log.warn(it) { "Failed to export backup" }
            }
            .singleOrNull()

        fun asFlow(block: HttpRequestBuilder.() -> Unit = {}) = backupRepository.exportBackupFile(block)

        companion object {
            private val log = logging()
        }
    }
