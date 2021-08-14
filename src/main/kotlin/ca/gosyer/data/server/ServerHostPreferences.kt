package ca.gosyer.data.server

import ca.gosyer.common.prefs.Preference
import ca.gosyer.common.prefs.PreferenceStore
import ca.gosyer.data.server.host.ServerHostPreference

class ServerHostPreferences(preferenceStore: PreferenceStore) {

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

    // WebUI
    private val webUIEnabled = ServerHostPreference.WebUIEnabled(preferenceStore)
    fun webUIEnabled(): Preference<Boolean> {
        return webUIEnabled.preference()
    }
    private val openInBrowserEnabled = ServerHostPreference.OpenInBrowserEnabled(preferenceStore)
    fun openInBrowserEnabled(): Preference<Boolean> {
        return openInBrowserEnabled.preference()
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
            webUIEnabled,
            openInBrowserEnabled
        ).mapNotNull {
            it.getProperty()
        }.toTypedArray()
    }
}
