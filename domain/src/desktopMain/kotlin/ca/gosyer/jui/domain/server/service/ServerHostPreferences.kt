/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.domain.server.service

import ca.gosyer.jui.core.prefs.Preference
import ca.gosyer.jui.core.prefs.PreferenceStore
import ca.gosyer.jui.domain.server.service.host.ServerHostPreference

actual class ServerHostPreferences actual constructor(
    private val preferenceStore: PreferenceStore,
) {
    actual fun host(): Preference<Boolean> = preferenceStore.getBoolean("host", true)

    private val ip = ServerHostPreference.IP(preferenceStore)

    fun ip(): Preference<String> = ip.preference()

    private val port = ServerHostPreference.Port(preferenceStore)

    fun port(): Preference<Int> = port.preference()

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

    fun basicAuthEnabled(): Preference<Boolean> = basicAuthEnabled.preference()

    private val basicAuthUsername = ServerHostPreference.BasicAuthUsername(preferenceStore)

    fun basicAuthUsername(): Preference<String> = basicAuthUsername.preference()

    private val basicAuthPassword = ServerHostPreference.BasicAuthPassword(preferenceStore)

    fun basicAuthPassword(): Preference<String> = basicAuthPassword.preference()

    fun properties(): Array<String> =
        listOf(
            ip,
            port,
            downloadPath,
            backupPath,
            localSourcePath,
            basicAuthEnabled,
            basicAuthUsername,
            basicAuthPassword,
        ).mapNotNull {
            it.getProperty()
        }.plus(
            "-Dsuwayomi.tachidesk.config.server.initialOpenInBrowserEnabled=false"
        ).toTypedArray()
}
