/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.data.server.interactions

import ca.gosyer.jui.core.lang.IO
import ca.gosyer.jui.data.models.About
import ca.gosyer.jui.data.server.Http
import ca.gosyer.jui.data.server.ServerPreferences
import ca.gosyer.jui.data.server.requests.aboutQuery
import ca.gosyer.jui.data.server.requests.checkUpdateQuery
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.statement.HttpResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import me.tatarka.inject.annotations.Inject

class SettingsInteractionHandler @Inject constructor(
    client: Http,
    serverPreferences: ServerPreferences
) : BaseInteractionHandler(client, serverPreferences) {

    fun aboutServer() = flow {
        val response = client.get<About>(
            serverUrl + aboutQuery()
        )
        emit(response)
    }.flowOn(Dispatchers.IO)

    fun checkUpdate() = flow {
        val response = client.post<HttpResponse>(
            serverUrl + checkUpdateQuery()
        )
        emit(response)
    }.flowOn(Dispatchers.IO)
}
