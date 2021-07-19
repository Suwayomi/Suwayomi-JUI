/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.data.server

import ca.gosyer.common.prefs.Preference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class ServerUrlPreference(
    private val key: String,
    private val server: Preference<String>,
    private val port: Preference<Int>
) : Preference<String> {
    override fun key(): String {
        return key
    }

    override fun get(): String {
        return server.get() + ":" + port.get()
    }

    override fun set(value: String) {
        val (server, port) = value.split(':')
        this.server.set(server)
        this.port.set(port.toInt())
    }

    override fun isSet(): Boolean {
        return server.isSet() || port.isSet()
    }

    override fun delete() {
        server.delete()
        port.delete()
    }

    override fun defaultValue(): String {
        return server.defaultValue() + ":" + port.defaultValue()
    }

    override fun changes(): Flow<String> {
        return combine(server.changes(), port.changes()) { server, port ->
            "$server:$port"
        }
    }

    override fun stateIn(scope: CoroutineScope): StateFlow<String> {
        return changes().stateIn(scope, SharingStarted.Eagerly, get())
    }
}
