/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.domain.library.service

import ca.gosyer.jui.domain.base.WebsocketService
import ca.gosyer.jui.domain.library.model.UpdateStatus
import ca.gosyer.jui.domain.server.Http
import ca.gosyer.jui.domain.server.service.ServerPreferences
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.flow.MutableStateFlow
import me.tatarka.inject.annotations.Inject
import org.lighthousegames.logging.logging

@Inject
class LibraryUpdateService(
    serverPreferences: ServerPreferences,
    client: Http,
) : WebsocketService(serverPreferences, client) {
    override val status: MutableStateFlow<Status>
        get() = LibraryUpdateService.status

    override val query: String
        get() = "/api/v1/update"

    override suspend fun onReceived(frame: Frame.Text) {
        updateStatus.value = json.decodeFromString<UpdateStatus>(frame.readText())
    }

    companion object {
        private val log = logging()

        val status = MutableStateFlow(Status.STARTING)
        val updateStatus = MutableStateFlow(UpdateStatus(emptyMap(), emptyMap(), false))
    }
}
