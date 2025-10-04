package ca.gosyer.jui.data.backup

import ca.gosyer.jui.core.io.toSource
import ca.gosyer.jui.data.graphql.CreateBackupMutation
import ca.gosyer.jui.data.graphql.RestoreBackupMutation
import ca.gosyer.jui.data.graphql.RestoreStatusQuery
import ca.gosyer.jui.data.graphql.ValidateBackupQuery
import ca.gosyer.jui.data.graphql.fragment.RestoreStatusFragment
import ca.gosyer.jui.data.graphql.type.BackupRestoreState
import ca.gosyer.jui.domain.backup.model.BackupValidationResult
import ca.gosyer.jui.domain.backup.model.RestoreState
import ca.gosyer.jui.domain.backup.model.RestoreStatus
import ca.gosyer.jui.domain.backup.service.BackupRepository
import ca.gosyer.jui.domain.server.Http
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.DefaultUpload
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.Url
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import okio.Source
import okio.buffer

class BackupRepositoryImpl(
    private val apolloClient: ApolloClient,
    private val http: Http,
    private val serverUrl: Url,
) : BackupRepository {
    override fun validateBackup(source: Source): Flow<BackupValidationResult> =
        apolloClient.query(
            ValidateBackupQuery(
                DefaultUpload.Builder()
                    .content {
                        it.writeAll(source.buffer())
                    }
                    .fileName("backup.tachibk")
                    .contentType("application/octet-stream")
                    .build(),
            ),
        ).toFlow()
            .map {
                BackupValidationResult(
                    missingSources = it.dataAssertNoErrors.validateBackup.missingSources.map { source ->
                        "${source.name} (${source.id})"
                    },
                    missingTrackers = emptyList(),
                )
            }

    override fun restoreBackup(source: Source): Flow<Pair<String, RestoreStatus>> =
        apolloClient.mutation(
            RestoreBackupMutation(
                DefaultUpload.Builder()
                    .content {
                        it.writeAll(source.buffer())
                    }
                    .fileName("backup.tachibk")
                    .contentType("application/octet-stream")
                    .build(),
            ),
        ).toFlow()
            .map {
                val data = it.dataAssertNoErrors
                data.restoreBackup.id to data.restoreBackup.status!!
                    .restoreStatusFragment
                    .toRestoreStatus()
            }

    override fun restoreStatus(id: String): Flow<RestoreStatus> =
        apolloClient.query(
            RestoreStatusQuery(id),
        ).toFlow()
            .map {
                val data = it.dataAssertNoErrors
                data.restoreStatus!!.restoreStatusFragment.toRestoreStatus()
            }

    override fun createBackup(
        includeCategories: Boolean,
        includeChapters: Boolean,
        block: HttpRequestBuilder.() -> Unit,
    ): Flow<Pair<String, Source>> =
        apolloClient
            .mutation(
                CreateBackupMutation(includeCategories, includeChapters),
            )
            .toFlow()
            .map {
                val url = it.dataAssertNoErrors.createBackup.url
                val response = http.get(
                    Url("$serverUrl$url"),
                )
                val fileName = response.headers["content-disposition"]!!
                    .substringAfter("filename=")
                    .trim('"')
                fileName to response.bodyAsChannel().toSource()
            }

    companion object {
        private fun RestoreStatusFragment.toRestoreStatus() =
            RestoreStatus(
                when (state) {
                    BackupRestoreState.IDLE -> RestoreState.IDLE
                    BackupRestoreState.SUCCESS -> RestoreState.SUCCESS
                    BackupRestoreState.FAILURE -> RestoreState.FAILURE
                    BackupRestoreState.RESTORING_CATEGORIES -> RestoreState.RESTORING_CATEGORIES
                    BackupRestoreState.RESTORING_MANGA -> RestoreState.RESTORING_MANGA
                    BackupRestoreState.RESTORING_META -> RestoreState.RESTORING_META
                    BackupRestoreState.RESTORING_SETTINGS -> RestoreState.RESTORING_SETTINGS
                    BackupRestoreState.UNKNOWN__ -> RestoreState.UNKNOWN
                },
                mangaProgress,
                totalManga,
            )
    }
}
