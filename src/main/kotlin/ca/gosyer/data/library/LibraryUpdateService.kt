/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.data.library

import ca.gosyer.core.service.WebsocketService
import ca.gosyer.data.library.model.UpdateStatus
import ca.gosyer.data.server.Http
import ca.gosyer.data.server.ServerPreferences
import ca.gosyer.data.server.requests.updatesQuery
import ca.gosyer.util.system.CKLogger
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.readText
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.serialization.decodeFromString
import javax.inject.Inject

@OptIn(DelicateCoroutinesApi::class)
class LibraryUpdateService @Inject constructor(
    serverPreferences: ServerPreferences,
    client: Http
) : WebsocketService(serverPreferences, client) {

    override val query: String
        get() = updatesQuery()

    override suspend fun onReceived(frame: Frame.Text) {
        val status = json.decodeFromString<UpdateStatus>(frame.readText())
        info { status }
    }

    private companion object : CKLogger({})
}
