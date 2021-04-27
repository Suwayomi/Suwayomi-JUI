/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.data.server

import ca.gosyer.BuildConfig
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.logging.LogLevel
import io.ktor.client.features.logging.Logging
import javax.inject.Inject
import javax.inject.Provider

typealias Http = HttpClient

internal class HttpProvider @Inject constructor() : Provider<Http> {
    override fun get(): Http {
        return HttpClient(OkHttp) {
            install(JsonFeature)
            if (BuildConfig.DEBUG) {
                install(Logging) {
                    level = LogLevel.HEADERS
                }
            }
        }
    }
}
