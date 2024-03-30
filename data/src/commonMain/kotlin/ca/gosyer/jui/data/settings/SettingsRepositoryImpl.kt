/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.data.settings

import ca.gosyer.jui.data.graphql.AllSettingsQuery
import ca.gosyer.jui.data.graphql.SetSettingsMutation
import ca.gosyer.jui.data.graphql.fragment.SettingsTypeFragment
import ca.gosyer.jui.data.graphql.type.WebUIChannel
import ca.gosyer.jui.data.graphql.type.WebUIFlavor
import ca.gosyer.jui.data.graphql.type.WebUIInterface
import ca.gosyer.jui.data.util.toOptional
import ca.gosyer.jui.domain.settings.model.SetSettingsInput
import ca.gosyer.jui.domain.settings.model.Settings
import ca.gosyer.jui.domain.settings.service.SettingsRepository
import com.apollographql.apollo3.ApolloClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import ca.gosyer.jui.domain.settings.model.WebUIChannel as DomainWebUIChannel
import ca.gosyer.jui.domain.settings.model.WebUIFlavor as DomainWebUIFlavor
import ca.gosyer.jui.domain.settings.model.WebUIInterface as DomainWebUIInterface

class SettingsRepositoryImpl(private val apolloClient: ApolloClient) : SettingsRepository {

    private fun SettingsTypeFragment.toSettings() = Settings(
        autoDownloadNewChapters = autoDownloadNewChapters,
        autoDownloadNewChaptersLimit = autoDownloadNewChaptersLimit,
        backupInterval = backupInterval,
        backupPath = backupPath,
        backupTTL = backupTTL,
        backupTime = backupTime,
        basicAuthEnabled = basicAuthEnabled,
        basicAuthPassword = basicAuthPassword,
        basicAuthUsername = basicAuthUsername,
        debugLogsEnabled = debugLogsEnabled,
        downloadAsCbz = downloadAsCbz,
        downloadsPath = downloadsPath,
        electronPath = electronPath,
        excludeCompleted = excludeCompleted,
        excludeEntryWithUnreadChapters = excludeEntryWithUnreadChapters,
        excludeNotStarted = excludeNotStarted,
        excludeUnreadChapters = excludeUnreadChapters,
        extensionRepos = extensionRepos,
        flareSolverrEnabled = flareSolverrEnabled,
        flareSolverrSessionName = flareSolverrSessionName,
        flareSolverrSessionTtl = flareSolverrSessionTtl,
        flareSolverrTimeout = flareSolverrTimeout,
        flareSolverrUrl = flareSolverrUrl,
        globalUpdateInterval = globalUpdateInterval,
        gqlDebugLogsEnabled = gqlDebugLogsEnabled,
        initialOpenInBrowserEnabled = initialOpenInBrowserEnabled,
        ip = ip,
        localSourcePath = localSourcePath,
        maxSourcesInParallel = maxSourcesInParallel,
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
        webUIUpdateCheckInterval = webUIUpdateCheckInterval
    )

    private fun WebUIChannel.toDomain() = when (this) {
        WebUIChannel.BUNDLED -> DomainWebUIChannel.BUNDLED
        WebUIChannel.STABLE -> DomainWebUIChannel.STABLE
        WebUIChannel.PREVIEW -> DomainWebUIChannel.PREVIEW
        WebUIChannel.UNKNOWN__ -> DomainWebUIChannel.UNKNOWN__
    }

    private fun WebUIFlavor.toDomain() = when (this) {
        WebUIFlavor.WEBUI -> DomainWebUIFlavor.WEBUI
        WebUIFlavor.VUI -> DomainWebUIFlavor.VUI
        WebUIFlavor.CUSTOM -> DomainWebUIFlavor.CUSTOM
        WebUIFlavor.UNKNOWN__ -> DomainWebUIFlavor.UNKNOWN__
    }

    private fun WebUIInterface.toDomain() = when (this) {
        WebUIInterface.BROWSER -> DomainWebUIInterface.BROWSER
        WebUIInterface.ELECTRON -> DomainWebUIInterface.ELECTRON
        WebUIInterface.UNKNOWN__ -> DomainWebUIInterface.UNKNOWN__
    }

    private fun DomainWebUIChannel.toGraphQL() = when (this) {
        DomainWebUIChannel.BUNDLED -> WebUIChannel.BUNDLED
        DomainWebUIChannel.STABLE -> WebUIChannel.STABLE
        DomainWebUIChannel.PREVIEW -> WebUIChannel.PREVIEW
        DomainWebUIChannel.UNKNOWN__ -> WebUIChannel.UNKNOWN__
    }

    private fun DomainWebUIFlavor.toGraphQL() = when (this) {
        DomainWebUIFlavor.WEBUI -> WebUIFlavor.WEBUI
        DomainWebUIFlavor.VUI -> WebUIFlavor.VUI
        DomainWebUIFlavor.CUSTOM -> WebUIFlavor.CUSTOM
        DomainWebUIFlavor.UNKNOWN__ -> WebUIFlavor.UNKNOWN__
    }

    private fun DomainWebUIInterface.toGraphQL() = when (this) {
        DomainWebUIInterface.BROWSER -> WebUIInterface.BROWSER
        DomainWebUIInterface.ELECTRON -> WebUIInterface.ELECTRON
        DomainWebUIInterface.UNKNOWN__ -> WebUIInterface.UNKNOWN__
    }

    override fun getSettings(): Flow<Settings> {
        return apolloClient.query(AllSettingsQuery()).toFlow()
            .map {
                it.dataOrThrow().settings.settingsTypeFragment.toSettings()
            }
            .flowOn(Dispatchers.IO)
    }


    private fun SetSettingsInput.toMutation() = SetSettingsMutation(
        autoDownloadNewChapters = autoDownloadNewChapters.toOptional(),
        autoDownloadNewChaptersLimit = autoDownloadNewChaptersLimit.toOptional(),
        backupInterval = backupInterval.toOptional(),
        backupPath = backupPath.toOptional(),
        backupTTL = backupTTL.toOptional(),
        backupTime = backupTime.toOptional(),
        basicAuthEnabled = basicAuthEnabled.toOptional(),
        basicAuthPassword = basicAuthPassword.toOptional(),
        basicAuthUsername = basicAuthUsername.toOptional(),
        debugLogsEnabled = debugLogsEnabled.toOptional(),
        downloadAsCbz = downloadAsCbz.toOptional(),
        downloadsPath = downloadsPath.toOptional(),
        electronPath = electronPath.toOptional(),
        excludeCompleted = excludeCompleted.toOptional(),
        excludeEntryWithUnreadChapters = excludeEntryWithUnreadChapters.toOptional(),
        excludeNotStarted = excludeNotStarted.toOptional(),
        excludeUnreadChapters = excludeUnreadChapters.toOptional(),
        extensionRepos = extensionRepos.toOptional(),
        flareSolverrEnabled = flareSolverrEnabled.toOptional(),
        flareSolverrSessionName = flareSolverrSessionName.toOptional(),
        flareSolverrSessionTtl = flareSolverrSessionTtl.toOptional(),
        flareSolverrTimeout = flareSolverrTimeout.toOptional(),
        flareSolverrUrl = flareSolverrUrl.toOptional(),
        globalUpdateInterval = globalUpdateInterval.toOptional(),
        gqlDebugLogsEnabled = gqlDebugLogsEnabled.toOptional(),
        initialOpenInBrowserEnabled = initialOpenInBrowserEnabled.toOptional(),
        ip = ip.toOptional(),
        localSourcePath = localSourcePath.toOptional(),
        maxSourcesInParallel = maxSourcesInParallel.toOptional(),
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

    override fun setSettings(input: SetSettingsInput): Flow<Unit> {
        return apolloClient.mutation(input.toMutation())
            .toFlow()
            .map {
                it.dataOrThrow()
                Unit
            }
            .flowOn(Dispatchers.IO)
    }
}
