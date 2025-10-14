/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.data.settings

import ca.gosyer.jui.data.ApolloAppClient
import ca.gosyer.jui.data.graphql.AboutServerQuery
import ca.gosyer.jui.data.graphql.AllSettingsQuery
import ca.gosyer.jui.data.graphql.SetSettingsMutation
import ca.gosyer.jui.data.graphql.fragment.SettingsTypeFragment
import ca.gosyer.jui.data.graphql.fragment.SettingsTypeFragment.DownloadConversion
import ca.gosyer.jui.data.graphql.type.AuthMode
import ca.gosyer.jui.data.graphql.type.DatabaseType
import ca.gosyer.jui.data.graphql.type.KoreaderSyncChecksumMethod
import ca.gosyer.jui.data.graphql.type.KoreaderSyncConflictStrategy
import ca.gosyer.jui.data.graphql.type.SettingsDownloadConversionTypeInput
import ca.gosyer.jui.data.graphql.type.SortOrder
import ca.gosyer.jui.data.graphql.type.WebUIChannel
import ca.gosyer.jui.data.graphql.type.WebUIFlavor
import ca.gosyer.jui.data.graphql.type.WebUIInterface
import ca.gosyer.jui.data.util.toOptional
import ca.gosyer.jui.domain.settings.model.About
import ca.gosyer.jui.domain.settings.model.AboutBuildType
import ca.gosyer.jui.domain.settings.model.SetSettingsInput
import ca.gosyer.jui.domain.settings.model.Settings
import ca.gosyer.jui.domain.settings.service.SettingsRepository
import com.apollographql.apollo.ApolloClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import ca.gosyer.jui.domain.settings.model.AuthMode as DomainAuthMode
import ca.gosyer.jui.domain.settings.model.DatabaseType as DomainDatabaseType
import ca.gosyer.jui.domain.settings.model.DownloadConversion as DomainDownloadConversion
import ca.gosyer.jui.domain.settings.model.KoreaderSyncChecksumMethod as DomainKoreaderSyncChecksumMethod
import ca.gosyer.jui.domain.settings.model.KoreaderSyncConflictStrategy as DomainKoreaderSyncConflictStrategy
import ca.gosyer.jui.domain.settings.model.SortOrder as DomainSortOrder
import ca.gosyer.jui.domain.settings.model.WebUIChannel as DomainWebUIChannel
import ca.gosyer.jui.domain.settings.model.WebUIFlavor as DomainWebUIFlavor
import ca.gosyer.jui.domain.settings.model.WebUIInterface as DomainWebUIInterface

class SettingsRepositoryImpl(
    private val apolloAppClient: ApolloAppClient,
) : SettingsRepository {
    val apolloClient: ApolloClient
        get() = apolloAppClient.value

    private fun SettingsTypeFragment.toSettings() =
        Settings(
            authMode = authMode.toDomain(),
            authPassword = authPassword,
            authUsername = authUsername,
            autoDownloadIgnoreReUploads = autoDownloadIgnoreReUploads,
            autoDownloadNewChapters = autoDownloadNewChapters,
            autoDownloadNewChaptersLimit = autoDownloadNewChaptersLimit,
            backupInterval = backupInterval,
            backupPath = backupPath,
            backupTTL = backupTTL,
            backupTime = backupTime,
            databasePassword = databasePassword,
            databaseType = databaseType.toDomain(),
            databaseUrl = databaseUrl,
            databaseUsername = databaseUsername,
            debugLogsEnabled = debugLogsEnabled,
            downloadAsCbz = downloadAsCbz,
            downloadConversions = downloadConversions.map { it.toDomain() },
            downloadsPath = downloadsPath,
            electronPath = electronPath,
            excludeCompleted = excludeCompleted,
            excludeEntryWithUnreadChapters = excludeEntryWithUnreadChapters,
            excludeNotStarted = excludeNotStarted,
            excludeUnreadChapters = excludeUnreadChapters,
            extensionRepos = extensionRepos,
            flareSolverrAsResponseFallback = flareSolverrAsResponseFallback,
            flareSolverrEnabled = flareSolverrEnabled,
            flareSolverrSessionName = flareSolverrSessionName,
            flareSolverrSessionTtl = flareSolverrSessionTtl,
            flareSolverrTimeout = flareSolverrTimeout,
            flareSolverrUrl = flareSolverrUrl,
            globalUpdateInterval = globalUpdateInterval,
            initialOpenInBrowserEnabled = initialOpenInBrowserEnabled,
            ip = ip,
            jwtAudience = jwtAudience,
            jwtRefreshExpiry = jwtRefreshExpiry,
            jwtTokenExpiry = jwtTokenExpiry,
            koreaderSyncChecksumMethod = koreaderSyncChecksumMethod.toDomain(),
            koreaderSyncDeviceId = koreaderSyncDeviceId,
            koreaderSyncPercentageTolerance = koreaderSyncPercentageTolerance,
            koreaderSyncServerUrl = koreaderSyncServerUrl,
            koreaderSyncStrategyBackward = koreaderSyncStrategyBackward.toDomain(),
            koreaderSyncStrategyForward = koreaderSyncStrategyForward.toDomain(),
            koreaderSyncUserkey = koreaderSyncUserkey,
            koreaderSyncUsername = koreaderSyncUsername,
            localSourcePath = localSourcePath,
            maxLogFileSize = maxLogFileSize,
            maxLogFiles = maxLogFiles,
            maxLogFolderSize = maxLogFolderSize,
            maxSourcesInParallel = maxSourcesInParallel,
            opdsChapterSortOrder = opdsChapterSortOrder.toDomain(),
            opdsEnablePageReadProgress = opdsEnablePageReadProgress,
            opdsItemsPerPage = opdsItemsPerPage,
            opdsMarkAsReadOnDownload = opdsMarkAsReadOnDownload,
            opdsShowOnlyDownloadedChapters = opdsShowOnlyDownloadedChapters,
            opdsShowOnlyUnreadChapters = opdsShowOnlyUnreadChapters,
            opdsUseBinaryFileSizes = opdsUseBinaryFileSizes,
            port = port,
            socksProxyEnabled = socksProxyEnabled,
            socksProxyHost = socksProxyHost,
            socksProxyPassword = socksProxyPassword,
            socksProxyPort = socksProxyPort,
            socksProxyUsername = socksProxyUsername,
            socksProxyVersion = socksProxyVersion,
            systemTrayEnabled = systemTrayEnabled,
            updateMangas = updateMangas,
            webUIChannel = webUIChannel.toDomain(),
            webUIFlavor = webUIFlavor.toDomain(),
            webUIInterface = webUIInterface.toDomain(),
            webUIUpdateCheckInterval = webUIUpdateCheckInterval,
        )

    private fun AuthMode.toDomain() =
        when (this) {
            AuthMode.NONE -> DomainAuthMode.NONE
            AuthMode.BASIC_AUTH -> DomainAuthMode.BASIC_AUTH
            AuthMode.SIMPLE_LOGIN -> DomainAuthMode.SIMPLE_LOGIN
            AuthMode.UI_LOGIN -> DomainAuthMode.UI_LOGIN
            AuthMode.UNKNOWN__ -> DomainAuthMode.UNKNOWN__
        }

    private fun DatabaseType.toDomain() =
        when (this) {
            DatabaseType.H2 -> DomainDatabaseType.H2
            DatabaseType.POSTGRESQL -> DomainDatabaseType.POSTGRESQL
            DatabaseType.UNKNOWN__ -> DomainDatabaseType.UNKNOWN__
        }

    private fun DownloadConversion.toDomain() =
        DomainDownloadConversion(
            compressionLevel = compressionLevel,
            mimeType = mimeType,
            target = target,
        )

    private fun KoreaderSyncConflictStrategy.toDomain() =
        when (this) {
            KoreaderSyncConflictStrategy.PROMPT -> DomainKoreaderSyncConflictStrategy.PROMPT
            KoreaderSyncConflictStrategy.KEEP_LOCAL -> DomainKoreaderSyncConflictStrategy.KEEP_LOCAL
            KoreaderSyncConflictStrategy.KEEP_REMOTE -> DomainKoreaderSyncConflictStrategy.KEEP_REMOTE
            KoreaderSyncConflictStrategy.DISABLED -> DomainKoreaderSyncConflictStrategy.DISABLED
            KoreaderSyncConflictStrategy.UNKNOWN__ -> DomainKoreaderSyncConflictStrategy.UNKNOWN__
        }

    private fun KoreaderSyncChecksumMethod.toDomain() =
        when (this) {
            KoreaderSyncChecksumMethod.BINARY -> DomainKoreaderSyncChecksumMethod.BINARY
            KoreaderSyncChecksumMethod.FILENAME -> DomainKoreaderSyncChecksumMethod.FILENAME
            KoreaderSyncChecksumMethod.UNKNOWN__ -> DomainKoreaderSyncChecksumMethod.UNKNOWN__
        }

    private fun SortOrder.toDomain() =
        when (this) {
            SortOrder.ASC -> DomainSortOrder.ASC
            SortOrder.DESC -> DomainSortOrder.DESC
            SortOrder.ASC_NULLS_FIRST -> DomainSortOrder.ASC_NULLS_FIRST
            SortOrder.DESC_NULLS_FIRST -> DomainSortOrder.DESC_NULLS_FIRST
            SortOrder.ASC_NULLS_LAST -> DomainSortOrder.ASC_NULLS_LAST
            SortOrder.DESC_NULLS_LAST -> DomainSortOrder.DESC_NULLS_LAST
            SortOrder.UNKNOWN__ -> DomainSortOrder.UNKNOWN__
        }

    private fun WebUIChannel.toDomain() =
        when (this) {
            WebUIChannel.BUNDLED -> DomainWebUIChannel.BUNDLED
            WebUIChannel.STABLE -> DomainWebUIChannel.STABLE
            WebUIChannel.PREVIEW -> DomainWebUIChannel.PREVIEW
            WebUIChannel.UNKNOWN__ -> DomainWebUIChannel.UNKNOWN__
        }

    private fun WebUIFlavor.toDomain() =
        when (this) {
            WebUIFlavor.WEBUI -> DomainWebUIFlavor.WEBUI
            WebUIFlavor.VUI -> DomainWebUIFlavor.VUI
            WebUIFlavor.CUSTOM -> DomainWebUIFlavor.CUSTOM
            WebUIFlavor.UNKNOWN__ -> DomainWebUIFlavor.UNKNOWN__
        }

    private fun WebUIInterface.toDomain() =
        when (this) {
            WebUIInterface.BROWSER -> DomainWebUIInterface.BROWSER
            WebUIInterface.ELECTRON -> DomainWebUIInterface.ELECTRON
            WebUIInterface.UNKNOWN__ -> DomainWebUIInterface.UNKNOWN__
        }

    private fun DomainAuthMode.toGraphQL() =
        when (this) {
            DomainAuthMode.NONE -> AuthMode.NONE
            DomainAuthMode.BASIC_AUTH -> AuthMode.BASIC_AUTH
            DomainAuthMode.SIMPLE_LOGIN -> AuthMode.SIMPLE_LOGIN
            DomainAuthMode.UI_LOGIN -> AuthMode.UI_LOGIN
            DomainAuthMode.UNKNOWN__ -> AuthMode.UNKNOWN__
        }

    private fun DomainDatabaseType.toGraphQL() =
        when (this) {
            DomainDatabaseType.H2 -> DatabaseType.H2
            DomainDatabaseType.POSTGRESQL -> DatabaseType.POSTGRESQL
            DomainDatabaseType.UNKNOWN__ -> DatabaseType.UNKNOWN__
        }

    private fun DomainDownloadConversion.toGraphQL() =
        SettingsDownloadConversionTypeInput(
            compressionLevel = compressionLevel.toOptional(),
            mimeType = mimeType,
            target = target,
        )

    private fun DomainKoreaderSyncChecksumMethod.toGraphQL() =
        when (this) {
            DomainKoreaderSyncChecksumMethod.BINARY -> KoreaderSyncChecksumMethod.BINARY
            DomainKoreaderSyncChecksumMethod.FILENAME -> KoreaderSyncChecksumMethod.FILENAME
            DomainKoreaderSyncChecksumMethod.UNKNOWN__ -> KoreaderSyncChecksumMethod.UNKNOWN__
        }

    private fun DomainKoreaderSyncConflictStrategy.toGraphQL() =
        when (this) {
            DomainKoreaderSyncConflictStrategy.PROMPT -> KoreaderSyncConflictStrategy.PROMPT
            DomainKoreaderSyncConflictStrategy.KEEP_LOCAL -> KoreaderSyncConflictStrategy.KEEP_LOCAL
            DomainKoreaderSyncConflictStrategy.KEEP_REMOTE -> KoreaderSyncConflictStrategy.KEEP_REMOTE
            DomainKoreaderSyncConflictStrategy.DISABLED -> KoreaderSyncConflictStrategy.DISABLED
            DomainKoreaderSyncConflictStrategy.UNKNOWN__ -> KoreaderSyncConflictStrategy.UNKNOWN__
        }

    private fun DomainSortOrder.toGraphQL() =
        when (this) {
            DomainSortOrder.ASC -> SortOrder.ASC
            DomainSortOrder.DESC -> SortOrder.DESC
            DomainSortOrder.ASC_NULLS_FIRST -> SortOrder.ASC_NULLS_FIRST
            DomainSortOrder.DESC_NULLS_FIRST -> SortOrder.DESC_NULLS_FIRST
            DomainSortOrder.ASC_NULLS_LAST -> SortOrder.ASC_NULLS_LAST
            DomainSortOrder.DESC_NULLS_LAST -> SortOrder.DESC_NULLS_LAST
            DomainSortOrder.UNKNOWN__ -> SortOrder.UNKNOWN__
        }

    private fun DomainWebUIChannel.toGraphQL() =
        when (this) {
            DomainWebUIChannel.BUNDLED -> WebUIChannel.BUNDLED
            DomainWebUIChannel.STABLE -> WebUIChannel.STABLE
            DomainWebUIChannel.PREVIEW -> WebUIChannel.PREVIEW
            DomainWebUIChannel.UNKNOWN__ -> WebUIChannel.UNKNOWN__
        }

    private fun DomainWebUIFlavor.toGraphQL() =
        when (this) {
            DomainWebUIFlavor.WEBUI -> WebUIFlavor.WEBUI
            DomainWebUIFlavor.VUI -> WebUIFlavor.VUI
            DomainWebUIFlavor.CUSTOM -> WebUIFlavor.CUSTOM
            DomainWebUIFlavor.UNKNOWN__ -> WebUIFlavor.UNKNOWN__
        }

    private fun DomainWebUIInterface.toGraphQL() =
        when (this) {
            DomainWebUIInterface.BROWSER -> WebUIInterface.BROWSER
            DomainWebUIInterface.ELECTRON -> WebUIInterface.ELECTRON
            DomainWebUIInterface.UNKNOWN__ -> WebUIInterface.UNKNOWN__
        }

    override fun getSettings(): Flow<Settings> =
        apolloClient.query(AllSettingsQuery()).toFlow()
            .map {
                it.dataOrThrow().settings.settingsTypeFragment.toSettings()
            }
            .flowOn(Dispatchers.IO)

    private fun SetSettingsInput.toMutation() =
        SetSettingsMutation(
            authMode = authMode?.toGraphQL().toOptional(),
            authPassword = authPassword.toOptional(),
            authUsername = authUsername.toOptional(),
            autoDownloadIgnoreReUploads = autoDownloadIgnoreReUploads.toOptional(),
            autoDownloadNewChapters = autoDownloadNewChapters.toOptional(),
            autoDownloadNewChaptersLimit = autoDownloadNewChaptersLimit.toOptional(),
            backupInterval = backupInterval.toOptional(),
            backupPath = backupPath.toOptional(),
            backupTTL = backupTTL.toOptional(),
            backupTime = backupTime.toOptional(),
            databasePassword = databasePassword.toOptional(),
            databaseType = databaseType?.toGraphQL().toOptional(),
            databaseUrl = databaseUrl.toOptional(),
            databaseUsername = databaseUsername.toOptional(),
            debugLogsEnabled = debugLogsEnabled.toOptional(),
            downloadAsCbz = downloadAsCbz.toOptional(),
            downloadConversions = downloadConversions?.map { it.toGraphQL() }.toOptional(),
            downloadsPath = downloadsPath.toOptional(),
            electronPath = electronPath.toOptional(),
            excludeCompleted = excludeCompleted.toOptional(),
            excludeEntryWithUnreadChapters = excludeEntryWithUnreadChapters.toOptional(),
            excludeNotStarted = excludeNotStarted.toOptional(),
            excludeUnreadChapters = excludeUnreadChapters.toOptional(),
            extensionRepos = extensionRepos.toOptional(),
            flareSolverrAsResponseFallback = flareSolverrAsResponseFallback.toOptional(),
            flareSolverrEnabled = flareSolverrEnabled.toOptional(),
            flareSolverrSessionName = flareSolverrSessionName.toOptional(),
            flareSolverrSessionTtl = flareSolverrSessionTtl.toOptional(),
            flareSolverrTimeout = flareSolverrTimeout.toOptional(),
            flareSolverrUrl = flareSolverrUrl.toOptional(),
            globalUpdateInterval = globalUpdateInterval.toOptional(),
            initialOpenInBrowserEnabled = initialOpenInBrowserEnabled.toOptional(),
            ip = ip.toOptional(),
            jwtAudience = jwtAudience.toOptional(),
            jwtRefreshExpiry = jwtRefreshExpiry.toOptional(),
            jwtTokenExpiry = jwtTokenExpiry.toOptional(),
            koreaderSyncChecksumMethod = koreaderSyncChecksumMethod?.toGraphQL().toOptional(),
            koreaderSyncDeviceId = koreaderSyncDeviceId.toOptional(),
            koreaderSyncPercentageTolerance = koreaderSyncPercentageTolerance.toOptional(),
            koreaderSyncServerUrl = koreaderSyncServerUrl.toOptional(),
            koreaderSyncStrategyBackward = koreaderSyncStrategyBackward?.toGraphQL().toOptional(),
            koreaderSyncStrategyForward = koreaderSyncStrategyForward?.toGraphQL().toOptional(),
            koreaderSyncUserkey = koreaderSyncUserkey.toOptional(),
            koreaderSyncUsername = koreaderSyncUsername.toOptional(),
            localSourcePath = localSourcePath.toOptional(),
            maxLogFileSize = maxLogFileSize.toOptional(),
            maxLogFiles = maxLogFiles.toOptional(),
            maxLogFolderSize = maxLogFolderSize.toOptional(),
            maxSourcesInParallel = maxSourcesInParallel.toOptional(),
            opdsChapterSortOrder = opdsChapterSortOrder?.toGraphQL().toOptional(),
            opdsEnablePageReadProgress = opdsEnablePageReadProgress.toOptional(),
            opdsItemsPerPage = opdsItemsPerPage.toOptional(),
            opdsMarkAsReadOnDownload = opdsMarkAsReadOnDownload.toOptional(),
            opdsShowOnlyDownloadedChapters = opdsShowOnlyDownloadedChapters.toOptional(),
            opdsShowOnlyUnreadChapters = opdsShowOnlyUnreadChapters.toOptional(),
            opdsUseBinaryFileSizes = opdsUseBinaryFileSizes.toOptional(),
            port = port.toOptional(),
            socksProxyEnabled = socksProxyEnabled.toOptional(),
            socksProxyHost = socksProxyHost.toOptional(),
            socksProxyPassword = socksProxyPassword.toOptional(),
            socksProxyPort = socksProxyPort.toOptional(),
            socksProxyUsername = socksProxyUsername.toOptional(),
            socksProxyVersion = socksProxyVersion.toOptional(),
            systemTrayEnabled = systemTrayEnabled.toOptional(),
            updateMangas = updateMangas.toOptional(),
            webUIChannel = webUIChannel?.toGraphQL().toOptional(),
            webUIFlavor = webUIFlavor?.toGraphQL().toOptional(),
            webUIInterface = webUIInterface?.toGraphQL().toOptional(),
            webUIUpdateCheckInterval = webUIUpdateCheckInterval.toOptional(),
        )

    override fun setSettings(input: SetSettingsInput): Flow<Unit> =
        apolloClient.mutation(input.toMutation())
            .toFlow()
            .map {
                it.dataOrThrow()
                Unit
            }
            .flowOn(Dispatchers.IO)

    override fun aboutServer(): Flow<About> =
        apolloClient.query(
            AboutServerQuery(),
        )
            .toFlow()
            .map {
                val data = it.dataAssertNoErrors
                About(
                    data.aboutServer.name,
                    data.aboutServer.version,
                    when (data.aboutServer.buildType) {
                        "Preview" -> AboutBuildType.Preview
                        else -> AboutBuildType.Stable
                    },
                    data.aboutServer.buildTime,
                    data.aboutServer.github,
                    data.aboutServer.discord,
                )
            }
            .flowOn(Dispatchers.IO)
}
