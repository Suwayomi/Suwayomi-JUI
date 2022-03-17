/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.data.library

import ca.gosyer.core.logging.CKLogger
import ca.gosyer.data.base.WebsocketService
import ca.gosyer.data.library.model.UpdateStatus
import ca.gosyer.data.server.Http
import ca.gosyer.data.server.ServerPreferences
import ca.gosyer.data.server.requests.updatesQuery
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.readText
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.decodeFromString
import me.tatarka.inject.annotations.Inject

@OptIn(DelicateCoroutinesApi::class)
class LibraryUpdateService @Inject constructor(
    serverPreferences: ServerPreferences,
    client: Http
) : WebsocketService(serverPreferences, client) {

    override val _status: MutableStateFlow<Status> = MutableStateFlow(Status.STARTING)

    override val query: String
        get() = updatesQuery()

    override suspend fun onReceived(frame: Frame.Text) {
        val status = json.decodeFromString<UpdateStatus>(frame.readText())
        info { status }
    }

    private companion object : CKLogger({})
}
