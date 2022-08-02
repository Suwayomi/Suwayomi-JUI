/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.domain.server

import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.darwin.Darwin
import io.ktor.client.engine.darwin.DarwinClientEngineConfig

actual val Engine: HttpClientEngineFactory<HttpClientEngineConfig>
    get() = Darwin

actual fun HttpClientConfig<HttpClientEngineConfig>.configurePlatform() {
    @Suppress("UNCHECKED_CAST")
    (this as HttpClientConfig<DarwinClientEngineConfig>).realConfigurePlatform()
}

private fun HttpClientConfig<DarwinClientEngineConfig>.realConfigurePlatform() {
    engine {
        configureRequest {
            setAllowsCellularAccess(true)
        }
    }
}
