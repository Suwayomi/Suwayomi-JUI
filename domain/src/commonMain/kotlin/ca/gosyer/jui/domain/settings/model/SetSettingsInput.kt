/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.domain.settings.model

class SetSettingsInput(
    val autoDownloadNewChapters: Boolean? = null,
    val autoDownloadNewChaptersLimit: Int? = null,
    val backupInterval: Int? = null,
    val backupPath: String? = null,
    val backupTTL: Int? = null,
    val backupTime: String? = null,
    val basicAuthEnabled: Boolean? = null,
    val basicAuthPassword: String? = null,
    val basicAuthUsername: String? = null,
    val debugLogsEnabled: Boolean? = null,
    val downloadAsCbz: Boolean? = null,
    val downloadsPath: String? = null,
    val electronPath: String? = null,
    val excludeCompleted: Boolean? = null,
    val excludeEntryWithUnreadChapters: Boolean? = null,
    val excludeNotStarted: Boolean? = null,
    val excludeUnreadChapters: Boolean? = null,
    val extensionRepos: List<String>? = null,
    val flareSolverrEnabled: Boolean? = null,
    val flareSolverrSessionName: String? = null,
    val flareSolverrSessionTtl: Int? = null,
    val flareSolverrTimeout: Int? = null,
    val flareSolverrUrl: String? = null,
    val globalUpdateInterval: Double? = null,
    val gqlDebugLogsEnabled: Boolean? = null,
    val initialOpenInBrowserEnabled: Boolean? = null,
    val ip: String? = null,
    val localSourcePath: String? = null,
    val maxSourcesInParallel: Int? = null,
    val port: Int? = null,
    val socksProxyEnabled: Boolean? = null,
    val socksProxyHost: String? = null,
    val socksProxyPassword: String? = null,
    val socksProxyPort: String? = null,
    val socksProxyUsername: String? = null,
    val socksProxyVersion: Int? = null,
    val systemTrayEnabled: Boolean? = null,
    val updateMangas: Boolean? = null,
    val webUIChannel: WebUIChannel? = null,
    val webUIFlavor: WebUIFlavor? = null,
    val webUIInterface: WebUIInterface? = null,
    val webUIUpdateCheckInterval: Double? = null,
)
