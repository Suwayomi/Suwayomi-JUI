/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.data.server

import ca.gosyer.core.prefs.Preference
import ca.gosyer.core.prefs.PreferenceStore
import ca.gosyer.data.server.model.Auth
import ca.gosyer.data.server.model.Proxy

class ServerPreferences(private val preferenceStore: PreferenceStore) {

    fun host(): Preference<Boolean> {
        return preferenceStore.getBoolean("host", true)
    }

    fun server(): Preference<String> {
        return preferenceStore.getString("server_url", "http://localhost")
    }

    fun port(): Preference<Int> {
        return preferenceStore.getInt("server_port", 4567)
    }

    fun serverUrl(): Preference<String> {
        return ServerUrlPreference("", server(), port())
    }

    fun proxy(): Preference<Proxy> {
        return preferenceStore.getJsonObject("proxy", Proxy.NO_PROXY, Proxy.serializer())
    }

    fun proxyHttpHost(): Preference<String> {
        return preferenceStore.getString("proxy_http_host")
    }

    fun proxyHttpPort(): Preference<Int> {
        return preferenceStore.getInt("proxy_http_port")
    }

    fun proxySocksHost(): Preference<String> {
        return preferenceStore.getString("proxy_socks_host")
    }

    fun proxySocksPort(): Preference<Int> {
        return preferenceStore.getInt("proxy_socks_port")
    }

    fun auth(): Preference<Auth> {
        return preferenceStore.getJsonObject("auth", Auth.NONE, Auth.serializer())
    }

    fun authUsername(): Preference<String> {
        return preferenceStore.getString("auth_username")
    }
    fun authPassword(): Preference<String> {
        return preferenceStore.getString("auth_password")
    }
}
