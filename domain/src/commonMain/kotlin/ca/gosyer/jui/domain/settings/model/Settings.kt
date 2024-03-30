/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.domain.settings.model

import androidx.compose.runtime.Stable

@Stable
class Settings(
    val autoDownloadNewChapters: Boolean,
    val autoDownloadNewChaptersLimit: Int,
    val backupInterval: Int,
    val backupPath: String,
    val backupTTL: Int,
    val backupTime: String,
    val basicAuthEnabled: Boolean,
    val basicAuthPassword: String,
    val basicAuthUsername: String,
    val debugLogsEnabled: Boolean,
    val downloadAsCbz: Boolean,
    val downloadsPath: String,
    val electronPath: String,
    val excludeCompleted: Boolean,
    val excludeEntryWithUnreadChapters: Boolean,
    val excludeNotStarted: Boolean,
    val excludeUnreadChapters: Boolean,
    val extensionRepos: List<String>,
    val flareSolverrEnabled: Boolean,
    val flareSolverrSessionName: String,
    val flareSolverrSessionTtl: Int,
    val flareSolverrTimeout: Int,
    val flareSolverrUrl: String,
    val globalUpdateInterval: Double,
    val gqlDebugLogsEnabled: Boolean,
    val initialOpenInBrowserEnabled: Boolean,
    val ip: String,
    val localSourcePath: String,
    val maxSourcesInParallel: Int,
    val port: Int,
    val socksProxyEnabled: Boolean,
    val socksProxyHost: String,
    val socksProxyPassword: String,
    val socksProxyPort: String,
    val socksProxyUsername: String,
    val socksProxyVersion: Int,
    val systemTrayEnabled: Boolean,
    val updateMangas: Boolean,
    val webUIChannel: WebUIChannel,
    val webUIFlavor: WebUIFlavor,
    val webUIInterface: WebUIInterface,
    val webUIUpdateCheckInterval: Double,
)
