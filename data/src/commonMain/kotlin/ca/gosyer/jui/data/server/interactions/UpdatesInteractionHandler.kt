/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.data.server.interactions

import ca.gosyer.jui.core.io.asSuccess
import ca.gosyer.jui.core.lang.IO
import ca.gosyer.jui.data.models.Category
import ca.gosyer.jui.data.models.Updates
import ca.gosyer.jui.data.server.Http
import ca.gosyer.jui.data.server.ServerPreferences
import ca.gosyer.jui.data.server.requests.fetchUpdatesRequest
import ca.gosyer.jui.data.server.requests.recentUpdatesQuery
import io.ktor.client.call.body
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.http.Parameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import me.tatarka.inject.annotations.Inject

class UpdatesInteractionHandler @Inject constructor(
    client: Http,
    serverPreferences: ServerPreferences
) : BaseInteractionHandler(client, serverPreferences) {

    fun getRecentUpdates(pageNum: Int) = flow {
        val response = client.get(
            serverUrl + recentUpdatesQuery(pageNum)
        ).asSuccess().body<Updates>()
        emit(response)
    }.flowOn(Dispatchers.IO)

    fun updateLibrary() = flow {
        val response = client.post(
            serverUrl + fetchUpdatesRequest()
        ).asSuccess()
        emit(response)
    }.flowOn(Dispatchers.IO)

    fun updateCategory(categoryId: Long) = flow {
        val response = client.submitForm(
            serverUrl + fetchUpdatesRequest(),
            formParameters = Parameters.build {
                append("category", categoryId.toString())
            }
        ).asSuccess()
        emit(response)
    }.flowOn(Dispatchers.IO)

    fun updateCategory(category: Category) = updateCategory(category.id)
}
