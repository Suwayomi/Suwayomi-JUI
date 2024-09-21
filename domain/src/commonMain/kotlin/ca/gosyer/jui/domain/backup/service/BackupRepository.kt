package ca.gosyer.jui.domain.backup.service

import ca.gosyer.jui.domain.backup.model.BackupValidationResult
import ca.gosyer.jui.domain.backup.model.RestoreStatus
import io.ktor.client.request.HttpRequestBuilder
import kotlinx.coroutines.flow.Flow
import okio.Source

interface BackupRepository {
    fun validateBackup(source: Source): Flow<BackupValidationResult>
    fun restoreBackup(source: Source): Flow<Pair<String, RestoreStatus>>
    fun restoreStatus(id: String): Flow<RestoreStatus>
    fun createBackup(
        includeCategories: Boolean,
        includeChapters: Boolean,
        block: HttpRequestBuilder.() -> Unit,
    ): Flow<Pair<String, Source>>
}
