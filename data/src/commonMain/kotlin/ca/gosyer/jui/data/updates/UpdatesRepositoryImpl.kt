/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.data.updates

import ca.gosyer.jui.core.lang.IO
import ca.gosyer.jui.data.base.BaseRepository
import ca.gosyer.jui.domain.server.Http
import ca.gosyer.jui.domain.server.model.requests.fetchUpdatesRequest
import ca.gosyer.jui.domain.server.model.requests.recentUpdatesQuery
import ca.gosyer.jui.domain.server.service.ServerPreferences
import ca.gosyer.jui.domain.updates.model.Updates
import ca.gosyer.jui.domain.updates.service.UpdatesRepository
import io.ktor.client.call.body
import io.ktor.client.plugins.expectSuccess
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.http.Parameters
import io.ktor.http.path
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import me.tatarka.inject.annotations.Inject

class UpdatesRepositoryImpl @Inject constructor(
    client: Http,
    serverPreferences: ServerPreferences
) : BaseRepository(client, serverPreferences), UpdatesRepository {

    override fun getRecentUpdates(pageNum: Int) = flow {
        val response = client.get(
            buildUrl { path(recentUpdatesQuery(pageNum)) }
        ) {
            expectSuccess = true
        }.body<Updates>()
        emit(response)
    }.flowOn(Dispatchers.IO)

    override fun updateLibrary() = flow {
        val response = client.post(
            buildUrl { path(fetchUpdatesRequest()) }
        ) {
            expectSuccess = true
        }
        emit(response)
    }.flowOn(Dispatchers.IO)

    override fun updateCategory(categoryId: Long) = flow {
        val response = client.submitForm(
            buildUrl { path(fetchUpdatesRequest()) },
            formParameters = Parameters.build {
                append("category", categoryId.toString())
            }
        ) {
            expectSuccess = true
        }
        emit(response)
    }.flowOn(Dispatchers.IO)
}
