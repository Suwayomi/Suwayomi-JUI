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
import com.diamondedge.logging.logging
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.ProxyBuilder
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.auth.providers.BasicAuthCredentials
import io.ktor.client.plugins.auth.providers.DigestAuthCredentials
import io.ktor.client.plugins.auth.providers.basic
import io.ktor.client.plugins.auth.providers.digest
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.http.URLBuilder
import io.ktor.http.URLProtocol
import io.ktor.http.Url
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import io.ktor.client.plugins.auth.Auth as AuthPlugin

typealias Http = StateFlow<HttpClient>

expect val Engine: HttpClientEngineFactory<HttpClientEngineConfig>

expect fun HttpClientConfig<HttpClientEngineConfig>.configurePlatform()

private fun getHttpClient(
    serverUrl: Url,
    proxy: Proxy,
    proxyHttpHost: String,
    proxyHttpPort: Int,
    proxySocksHost: String,
    proxySocksPort: Int,
    auth: Auth,
    authUsername: String,
    authPassword: String,
    json: Json
): HttpClient {
    return HttpClient(Engine) {
        configurePlatform()

        expectSuccess = true

        defaultRequest {
            url(serverUrl.toString())
        }

        engine {
            this.proxy = when (proxy) {
                Proxy.NO_PROXY -> null

                Proxy.HTTP_PROXY -> ProxyBuilder.http(
                    URLBuilder(
                        host = proxyHttpHost,
                        port = proxyHttpPort,
                    ).build(),
                )

                Proxy.SOCKS_PROXY -> ProxyBuilder.socks(
                    proxySocksHost,
                    proxySocksPort,
                )
            }
        }
        when (auth) {
            Auth.NONE -> Unit

            Auth.BASIC -> AuthPlugin {
                basic {
                    sendWithoutRequest {
                        it.url.protocol == URLProtocol.WS || it.url.protocol == URLProtocol.WSS
                    }
                    credentials {
                        BasicAuthCredentials(
                            authUsername,
                            authPassword,
                        )
                    }
                }
            }

            Auth.DIGEST -> AuthPlugin {
                digest {
                    credentials {
                        DigestAuthCredentials(
                            authUsername,
                            authPassword,
                        )
                    }
                }
            }
        }
        install(HttpTimeout) {
            connectTimeoutMillis = 30.seconds.inWholeMilliseconds
            requestTimeoutMillis = 30.seconds.inWholeMilliseconds
            socketTimeoutMillis = 2.minutes.inWholeMilliseconds
        }
        install(ContentNegotiation) {
            json(json)
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

@OptIn(DelicateCoroutinesApi::class)
fun httpClient(
    serverPreferences: ServerPreferences,
    json: Json,
): Http = combine(
    serverPreferences.serverUrl().stateIn(GlobalScope),
    serverPreferences.proxy().stateIn(GlobalScope),
    serverPreferences.proxyHttpHost().stateIn(GlobalScope),
    serverPreferences.proxyHttpPort().stateIn(GlobalScope),
    serverPreferences.proxySocksHost().stateIn(GlobalScope),
    serverPreferences.proxySocksPort().stateIn(GlobalScope),
    serverPreferences.auth().stateIn(GlobalScope),
    serverPreferences.authUsername().stateIn(GlobalScope),
    serverPreferences.authPassword().stateIn(GlobalScope),
) {
    getHttpClient(
        it[0] as Url,
        it[1] as Proxy,
        it[2] as String,
        it[3] as Int,
        it[4] as String,
        it[5] as Int,
        it[6] as Auth,
        it[7] as String,
        it[8] as String,
        json,
    )
}.stateIn(
    GlobalScope,
    SharingStarted.Eagerly,
    getHttpClient(
        serverPreferences.serverUrl().get(),
        serverPreferences.proxy().get(),
        serverPreferences.proxyHttpHost().get(),
        serverPreferences.proxyHttpPort().get(),
        serverPreferences.proxySocksHost().get(),
        serverPreferences.proxySocksPort().get(),
        serverPreferences.auth().get(),
        serverPreferences.authUsername().get(),
        serverPreferences.authPassword().get(),
        json,
    )
)
