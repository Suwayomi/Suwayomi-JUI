/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.domain.server.model

import ca.gosyer.jui.core.prefs.Preference
import ca.gosyer.jui.core.prefs.getAsFlow
import io.ktor.http.URLBuilder
import io.ktor.http.Url
import io.ktor.http.path
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.stateIn

class ServerUrlPreference(
    private val key: String,
    private val server: Preference<String>,
    private val port: Preference<Int>,
    private val pathPrefix: Preference<String>
) : Preference<Url> {
    override fun key(): String {
        return key
    }

    override fun get(): Url {
        return URLBuilder(server.get()).apply {
            port = this@ServerUrlPreference.port.get()
            if (pathPrefix.isSet()) {
                pathPrefix.get().ifBlank { null }?.let { path(it) }
            }
        }.build()
    }

    override fun set(value: Url) {
        server.set(value.protocol.name + "://" + value.host)
        port.set(value.port)
        pathPrefix.set(value.encodedPath)
    }

    override fun isSet(): Boolean {
        return server.isSet() || port.isSet() || pathPrefix.isSet()
    }

    override fun delete() {
        server.delete()
        port.delete()
        pathPrefix.delete()
    }

    override fun defaultValue(): Url {
        return URLBuilder(server.defaultValue()).apply {
            port = this@ServerUrlPreference.port.defaultValue()
        }.build()
    }

    override fun changes(): Flow<Url> {
        return combine(server.getAsFlow(), port.getAsFlow(), pathPrefix.getAsFlow()) { server, port, pathPrefix ->
            URLBuilder(server).apply {
                this.port = port
                if (pathPrefix.isNotBlank()) {
                    path(pathPrefix)
                }
            }.build()
        }.drop(1)
    }

    override fun stateIn(scope: CoroutineScope): StateFlow<Url> {
        return changes().stateIn(scope, SharingStarted.Eagerly, get())
    }
}
