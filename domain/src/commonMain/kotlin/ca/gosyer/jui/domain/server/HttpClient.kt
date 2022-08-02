/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.domain.server

import ca.gosyer.jui.domain.build.BuildKonfig
import ca.gosyer.jui.domain.server.model.Auth
import ca.gosyer.jui.domain.server.model.Proxy
import ca.gosyer.jui.domain.server.service.ServerPreferences
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.ProxyBuilder
import io.ktor.client.plugins.auth.providers.BasicAuthCredentials
import io.ktor.client.plugins.auth.providers.DigestAuthCredentials
import io.ktor.client.plugins.auth.providers.basic
import io.ktor.client.plugins.auth.providers.digest
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.http.URLBuilder
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import me.tatarka.inject.annotations.Inject
import org.lighthousegames.logging.logging
import io.ktor.client.plugins.auth.Auth as AuthPlugin

typealias Http = HttpClient

expect val Engine: HttpClientEngineFactory<HttpClientEngineConfig>

expect fun HttpClientConfig<HttpClientEngineConfig>.configurePlatform()

class HttpProvider @Inject constructor() {
    fun get(serverPreferences: ServerPreferences): Http {
        return HttpClient(Engine) {
            configurePlatform()

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
                Auth.BASIC -> AuthPlugin {
                    basic {
                        credentials {
                            BasicAuthCredentials(
                                serverPreferences.authUsername().get(),
                                serverPreferences.authPassword().get()
                            )
                        }
                    }
                }
                Auth.DIGEST -> AuthPlugin {
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
            install(ContentNegotiation) {
                json(
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
                logger = object : Logger {
                    val log = logging("HttpClient")
                    override fun log(message: String) {
                        log.info { message }
                    }
                }
            }
        }
    }
}
