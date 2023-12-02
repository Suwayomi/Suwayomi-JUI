/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.domain.server.service

import ca.gosyer.jui.core.prefs.Preference
import ca.gosyer.jui.core.prefs.PreferenceStore
import ca.gosyer.jui.domain.server.model.Auth
import ca.gosyer.jui.domain.server.model.Proxy
import ca.gosyer.jui.domain.server.model.ServerUrlPreference
import io.ktor.http.Url

class ServerPreferences(
    private val preferenceStore: PreferenceStore,
) {
    fun server(): Preference<String> = preferenceStore.getString("server_url", "http://localhost")

    fun port(): Preference<Int> = preferenceStore.getInt("server_port", 4567)

    fun pathPrefix(): Preference<String> = preferenceStore.getString("server_path_prefix", "")

    fun serverUrl(): Preference<Url> = ServerUrlPreference("", server(), port(), pathPrefix())

    fun proxy(): Preference<Proxy> = preferenceStore.getJsonObject("proxy", Proxy.NO_PROXY, Proxy.serializer())

    fun proxyHttpHost(): Preference<String> = preferenceStore.getString("proxy_http_host")

    fun proxyHttpPort(): Preference<Int> = preferenceStore.getInt("proxy_http_port")

    fun proxySocksHost(): Preference<String> = preferenceStore.getString("proxy_socks_host")

    fun proxySocksPort(): Preference<Int> = preferenceStore.getInt("proxy_socks_port")

    fun auth(): Preference<Auth> = preferenceStore.getJsonObject("auth", Auth.NONE, Auth.serializer())

    fun authUsername(): Preference<String> = preferenceStore.getString("auth_username")

    fun authPassword(): Preference<String> = preferenceStore.getString("auth_password")
}
