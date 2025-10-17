/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.domain.server

import ca.gosyer.jui.core.prefs.Preference
import ca.gosyer.jui.domain.build.BuildKonfig
import ca.gosyer.jui.domain.server.model.Auth
import ca.gosyer.jui.domain.server.model.Proxy
import ca.gosyer.jui.domain.server.service.ServerPreferences
import ca.gosyer.jui.domain.user.interactor.UserRefreshUI
import ca.gosyer.jui.domain.user.service.UserPreferences
import com.diamondedge.logging.logging
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.ProxyBuilder
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.api.Send
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.plugins.auth.providers.BasicAuthCredentials
import io.ktor.client.plugins.auth.providers.basic
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.http.HttpStatusCode
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

typealias HttpNoAuth = StateFlow<HttpClient>

expect val Engine: HttpClientEngineFactory<HttpClientEngineConfig>

expect fun HttpClientConfig<HttpClientEngineConfig>.configurePlatform()

private val log = logging()

private class SimpleAuthPluginConfig {
    var simpleSessionPreference: Preference<String>? = null
}

private val SimpleAuthPlugin = createClientPlugin("SimpleAuthPlugin", ::SimpleAuthPluginConfig) {
    val simpleSessionPreference = pluginConfig.simpleSessionPreference!!

    on(Send) { request ->
        val session = simpleSessionPreference.get()
        if (session.isNotEmpty()) {
            request.headers.append("Cookie", "JSESSIONID=$session")
        }
        proceed(request)
    }
}

private class UIAuthPluginConfig {
    var uiAccessTokenPreference: Preference<String>? = null
    var userRefreshUI: Lazy<UserRefreshUI>? = null
}

private val UIAuthPlugin = createClientPlugin("UIAuthPlugin", ::UIAuthPluginConfig) {
    val uiAccessTokenPreference = pluginConfig.uiAccessTokenPreference!!
    val userRefreshUI = pluginConfig.userRefreshUI!!

    on(Send) { request ->
        val token = uiAccessTokenPreference.get()
        if (token.isNotEmpty()) {
            request.headers.append("Authorization", "Bearer $token")
            val originalCall = proceed(request)
            if (originalCall.response.status == HttpStatusCode.Unauthorized) {
                log.warn { "Token expired, refreshing..." }
                val newToken = userRefreshUI.value.await()
                if (newToken != null) {
                    request.headers.remove("Authorization")
                    request.headers.append("Authorization", "Bearer $newToken")
                    proceed(request)
                } else {
                    originalCall
                }
            } else {
                originalCall
            }
        } else {
            proceed(request)
        }
    }
}



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
    uiAccessTokenPreference: Preference<String>,
    simpleSessionPreference: Preference<String>,
    userRefreshUI: Lazy<UserRefreshUI>,
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
            Auth.NONE, Auth.DIGEST -> Unit

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

            Auth.SIMPLE -> install(SimpleAuthPlugin) {
                this.simpleSessionPreference = simpleSessionPreference
            }
            Auth.UI -> install(UIAuthPlugin) {
                this.uiAccessTokenPreference = uiAccessTokenPreference
                this.userRefreshUI = userRefreshUI
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
    userPreferences: UserPreferences,
    userRefreshUI: Lazy<UserRefreshUI>,
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
        serverUrl = it[0] as Url,
        proxy = it[1] as Proxy,
        proxyHttpHost = it[2] as String,
        proxyHttpPort = it[3] as Int,
        proxySocksHost = it[4] as String,
        proxySocksPort = it[5] as Int,
        auth = it[6] as Auth,
        authUsername = it[7] as String,
        authPassword = it[8] as String,
        uiAccessTokenPreference = userPreferences.uiAccessToken(),
        simpleSessionPreference = userPreferences.simpleSession(),
        userRefreshUI = userRefreshUI,
        json = json,
    )
}.stateIn(
    GlobalScope,
    SharingStarted.Eagerly,
    getHttpClient(
        serverUrl = serverPreferences.serverUrl().get(),
        proxy = serverPreferences.proxy().get(),
        proxyHttpHost = serverPreferences.proxyHttpHost().get(),
        proxyHttpPort = serverPreferences.proxyHttpPort().get(),
        proxySocksHost = serverPreferences.proxySocksHost().get(),
        proxySocksPort = serverPreferences.proxySocksPort().get(),
        auth = serverPreferences.auth().get(),
        authUsername = serverPreferences.authUsername().get(),
        authPassword = serverPreferences.authPassword().get(),
        uiAccessTokenPreference = userPreferences.uiAccessToken(),
        simpleSessionPreference = userPreferences.simpleSession(),
        userRefreshUI = userRefreshUI,
        json = json,
    )
)

@OptIn(DelicateCoroutinesApi::class)
fun httpClientNoAuth(
    serverPreferences: ServerPreferences,
    userPreferences: UserPreferences,
    userRefreshUI: Lazy<UserRefreshUI>,
    json: Json,
): Http = combine(
    serverPreferences.serverUrl().stateIn(GlobalScope),
    serverPreferences.proxy().stateIn(GlobalScope),
    serverPreferences.proxyHttpHost().stateIn(GlobalScope),
    serverPreferences.proxyHttpPort().stateIn(GlobalScope),
    serverPreferences.proxySocksHost().stateIn(GlobalScope),
    serverPreferences.proxySocksPort().stateIn(GlobalScope),
) {
    getHttpClient(
        serverUrl = it[0] as Url,
        proxy = it[1] as Proxy,
        proxyHttpHost = it[2] as String,
        proxyHttpPort = it[3] as Int,
        proxySocksHost = it[4] as String,
        proxySocksPort = it[5] as Int,
        auth = Auth.NONE,
        authUsername = "",
        authPassword = "",
        uiAccessTokenPreference = userPreferences.uiAccessToken(),
        simpleSessionPreference = userPreferences.simpleSession(),
        userRefreshUI = userRefreshUI,
        json = json,
    )
}.stateIn(
    GlobalScope,
    SharingStarted.Eagerly,
    getHttpClient(
        serverUrl = serverPreferences.serverUrl().get(),
        proxy = serverPreferences.proxy().get(),
        proxyHttpHost = serverPreferences.proxyHttpHost().get(),
        proxyHttpPort = serverPreferences.proxyHttpPort().get(),
        proxySocksHost = serverPreferences.proxySocksHost().get(),
        proxySocksPort = serverPreferences.proxySocksPort().get(),
        auth = Auth.NONE,
        authUsername = "",
        authPassword = "",
        uiAccessTokenPreference = userPreferences.uiAccessToken(),
        simpleSessionPreference = userPreferences.simpleSession(),
        userRefreshUI = userRefreshUI,
        json = json,
    )
)
