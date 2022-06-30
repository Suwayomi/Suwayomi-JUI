/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.domain.server.service

import ca.gosyer.jui.core.prefs.Preference
import ca.gosyer.jui.core.prefs.PreferenceStore
import ca.gosyer.jui.domain.server.service.host.ServerHostPreference

class ServerHostPreferences(private val preferenceStore: PreferenceStore) {

    fun host(): Preference<Boolean> {
        return preferenceStore.getBoolean("host", true)
    }

    private val ip = ServerHostPreference.IP(preferenceStore)
    fun ip(): Preference<String> {
        return ip.preference()
    }
    private val port = ServerHostPreference.Port(preferenceStore)
    fun port(): Preference<Int> {
        return port.preference()
    }

    // Proxy
    private val socksProxyEnabled = ServerHostPreference.SocksProxyEnabled(preferenceStore)
    fun socksProxyEnabled(): Preference<Boolean> {
        return socksProxyEnabled.preference()
    }
    private val socksProxyHost = ServerHostPreference.SocksProxyHost(preferenceStore)
    fun socksProxyHost(): Preference<String> {
        return socksProxyHost.preference()
    }
    private val socksProxyPort = ServerHostPreference.SocksProxyPort(preferenceStore)
    fun socksProxyPort(): Preference<Int> {
        return socksProxyPort.preference()
    }

    // Misc
    private val debugLogsEnabled = ServerHostPreference.DebugLogsEnabled(preferenceStore)
    fun debugLogsEnabled(): Preference<Boolean> {
        return debugLogsEnabled.preference()
    }
    private val systemTrayEnabled = ServerHostPreference.SystemTrayEnabled(preferenceStore)
    fun systemTrayEnabled(): Preference<Boolean> {
        return systemTrayEnabled.preference()
    }
    private val downloadPath = ServerHostPreference.DownloadPath(preferenceStore)
    fun downloadPath(): Preference<String> {
        return downloadPath.preference()
    }

    // WebUI
    private val webUIEnabled = ServerHostPreference.WebUIEnabled(preferenceStore)
    fun webUIEnabled(): Preference<Boolean> {
        return webUIEnabled.preference()
    }
    private val openInBrowserEnabled = ServerHostPreference.OpenInBrowserEnabled(preferenceStore)
    fun openInBrowserEnabled(): Preference<Boolean> {
        return openInBrowserEnabled.preference()
    }

    // Authentication
    private val basicAuthEnabled = ServerHostPreference.BasicAuthEnabled(preferenceStore)
    fun basicAuthEnabled(): Preference<Boolean> {
        return basicAuthEnabled.preference()
    }
    private val basicAuthUsername = ServerHostPreference.BasicAuthUsername(preferenceStore)
    fun basicAuthUsername(): Preference<String> {
        return basicAuthUsername.preference()
    }
    private val basicAuthPassword = ServerHostPreference.BasicAuthPassword(preferenceStore)
    fun basicAuthPassword(): Preference<String> {
        return basicAuthPassword.preference()
    }

    fun properties(): Array<String> {
        return listOf(
            ip,
            port,
            socksProxyEnabled,
            socksProxyHost,
            socksProxyPort,
            debugLogsEnabled,
            systemTrayEnabled,
            downloadPath,
            webUIEnabled,
            openInBrowserEnabled,
            basicAuthEnabled,
            basicAuthUsername,
            basicAuthPassword
        ).mapNotNull {
            it.getProperty()
        }.toTypedArray()
    }
}
