/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.data.settings

import ca.gosyer.jui.core.lang.IO
import ca.gosyer.jui.data.base.BaseRepository
import ca.gosyer.jui.domain.server.Http
import ca.gosyer.jui.domain.server.model.requests.aboutQuery
import ca.gosyer.jui.domain.server.model.requests.checkUpdateQuery
import ca.gosyer.jui.domain.server.service.ServerPreferences
import ca.gosyer.jui.domain.settings.model.About
import ca.gosyer.jui.domain.settings.service.SettingsRepository
import io.ktor.client.call.body
import io.ktor.client.plugins.expectSuccess
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.http.path
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import me.tatarka.inject.annotations.Inject

class SettingsRepositoryImpl @Inject constructor(
    client: Http,
    serverPreferences: ServerPreferences
) : BaseRepository(client, serverPreferences), SettingsRepository {

    override fun aboutServer() = flow {
        val response = client.get(
            buildUrl { path(aboutQuery()) },
        ) {
            expectSuccess = true
        }.body<About>()
        emit(response)
    }.flowOn(Dispatchers.IO)

    override fun checkUpdate() = flow {
        val response = client.post(
            buildUrl { path(checkUpdateQuery()) },
        ) {
            expectSuccess = true
        }
        emit(response)
    }.flowOn(Dispatchers.IO)
}
