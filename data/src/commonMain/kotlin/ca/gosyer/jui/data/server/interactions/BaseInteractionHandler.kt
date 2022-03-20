/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.data.server.interactions

import ca.gosyer.jui.data.server.Http
import ca.gosyer.jui.data.server.ServerPreferences

open class BaseInteractionHandler(
    protected val client: Http,
    serverPreferences: ServerPreferences
) {
    private val _serverUrl = serverPreferences.serverUrl()
    val serverUrl get() = _serverUrl.get().toString()
}
