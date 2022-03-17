/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.data.server

import ca.gosyer.data.build.BuildKonfig
import ca.gosyer.data.server.model.Auth
import ca.gosyer.data.server.model.Proxy
import io.ktor.client.HttpClient
import io.ktor.client.engine.ProxyBuilder
import io.ktor.client.features.auth.providers.BasicAuthCredentials
import io.ktor.client.features.auth.providers.DigestAuthCredentials
import io.ktor.client.features.auth.providers.basic
import io.ktor.client.features.auth.providers.digest
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.features.logging.LogLevel
import io.ktor.client.features.logging.Logging
import io.ktor.client.features.websocket.WebSockets
import io.ktor.http.URLBuilder
import kotlinx.serialization.json.Json
import me.tatarka.inject.annotations.Inject
import io.ktor.client.features.auth.Auth as AuthFeature

typealias Http = HttpClient

class HttpProvider @Inject constructor() {
    fun get(serverPreferences: ServerPreferences): Http {
        return HttpClient {
            engine {
                proxy = when (serverPreferences.proxy().get()) {
                    Proxy.NO_PROXY -> null
                    Proxy.HTTP_PROXY -> ProxyBuilder.http(
                        URLBuilder(
                            host = serverPreferences.proxyHttpHost().get(),
                            port = serverPreferences.proxyHttpPort().get()
                        ).build()
                    )
                    Proxy.SOCKS_PROXY -> ProxyBuilder.socks(
                        serverPreferences.proxySocksHost().get(),
                        serverPreferences.proxySocksPort().get()
                    )
                }
            }
            when (serverPreferences.auth().get()) {
                Auth.NONE -> Unit
                Auth.BASIC -> install(AuthFeature) {
                    basic {
                        credentials {
                            BasicAuthCredentials(
                                serverPreferences.authUsername().get(),
                                serverPreferences.authPassword().get()
                            )
                        }
                    }
                }
                Auth.DIGEST -> install(AuthFeature) {
                    digest {
                        credentials {
                            DigestAuthCredentials(
                                serverPreferences.authUsername().get(),
                                serverPreferences.authPassword().get()
                            )
                        }
                    }
                }
            }
            install(JsonFeature) {
                serializer = KotlinxSerializer(
                    Json {
                        isLenient = false
                        ignoreUnknownKeys = true
                        allowSpecialFloatingPointValues = true
                        useArrayPolymorphism = false
                    }
                )
            }
            install(WebSockets)
            install(Logging) {
                level = if (BuildKonfig.DEBUG) {
                    LogLevel.HEADERS
                } else {
                    LogLevel.INFO
                }
            }
        }
    }
}
