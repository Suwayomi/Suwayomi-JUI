/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.domain.server.service

import ca.gosyer.jui.core.prefs.Preference
import ca.gosyer.jui.core.prefs.PreferenceStore
import ca.gosyer.jui.domain.server.service.host.ServerHostPreference
import ca.gosyer.jui.domain.settings.model.AuthMode

actual class ServerHostPreferences actual constructor(
    private val preferenceStore: PreferenceStore,
) {
    actual fun host(): Preference<Boolean> = preferenceStore.getBoolean("host", true)

    // IP
    private val ip = ServerHostPreference.IP(preferenceStore)
    fun ip(): Preference<String> = ip.preference()

    private val port = ServerHostPreference.Port(preferenceStore)
    fun port(): Preference<Int> = port.preference()

    // Root
    private val rootPath = ServerHostPreference.RootPath(preferenceStore)
    fun rootPath(): Preference<String> = rootPath.preference()

    // Downloader
    private val downloadPath = ServerHostPreference.DownloadPath(preferenceStore)
    fun downloadPath(): Preference<String> = downloadPath.preference()

    // Backup
    private val backupPath = ServerHostPreference.BackupPath(preferenceStore)
    fun backupPath(): Preference<String> = backupPath.preference()

    // LocalSource
    private val localSourcePath = ServerHostPreference.LocalSourcePath(preferenceStore)
    fun localSourcePath(): Preference<String> = localSourcePath.preference()

    // Authentication
    private val basicAuthEnabled = ServerHostPreference.BasicAuthEnabled(preferenceStore)
    @Deprecated("")
    fun basicAuthEnabled(): Preference<Boolean> = basicAuthEnabled.preference()
    private val basicAuthUsername = ServerHostPreference.BasicAuthUsername(preferenceStore)
    @Deprecated("")
    fun basicAuthUsername(): Preference<String> = basicAuthUsername.preference()
    private val basicAuthPassword = ServerHostPreference.BasicAuthPassword(preferenceStore)
    @Deprecated("")
    fun basicAuthPassword(): Preference<String> = basicAuthPassword.preference()

    // Authentication
    private val authMode = ServerHostPreference.AuthMode(preferenceStore)
    fun authMode(): Preference<AuthMode> = authMode.preference()

    private val authUsername = ServerHostPreference.AuthUsername(preferenceStore)
    fun authUsername(): Preference<String> = authUsername.preference()

    private val authPassword = ServerHostPreference.AuthPassword(preferenceStore)
    fun authPassword(): Preference<String> = authPassword.preference()

    fun properties(): Array<String> =
        listOf(
            ip,
            port,
            rootPath,
            downloadPath,
            backupPath,
            localSourcePath,
            authMode,
            authUsername,
            authPassword,
        ).mapNotNull {
            it.getProperty()
        }.plus(
            "-Dsuwayomi.tachidesk.config.server.initialOpenInBrowserEnabled=false",
        ).toTypedArray()
}
