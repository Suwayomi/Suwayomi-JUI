/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.data.server.interactions

import ca.gosyer.core.lang.withIOContext
import ca.gosyer.data.models.Category
import ca.gosyer.data.models.Updates
import ca.gosyer.data.server.Http
import ca.gosyer.data.server.ServerPreferences
import ca.gosyer.data.server.requests.fetchUpdatesRequest
import ca.gosyer.data.server.requests.recentUpdatesQuery
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.statement.HttpResponse
import io.ktor.http.Parameters
import javax.inject.Inject

class UpdatesInteractionHandler @Inject constructor(
    client: Http,
    serverPreferences: ServerPreferences
) : BaseInteractionHandler(client, serverPreferences) {

    suspend fun getRecentUpdates(pageNum: Int) = withIOContext {
        client.get<Updates>(
            serverUrl + recentUpdatesQuery(pageNum)
        )
    }

    suspend fun updateLibrary() = withIOContext {
        client.post<HttpResponse>(
            serverUrl + fetchUpdatesRequest()
        )
    }

    suspend fun updateCategory(categoryId: Long) = withIOContext {
        client.submitForm<HttpResponse>(
            serverUrl + fetchUpdatesRequest(),
            formParameters = Parameters.build {
                append("category", categoryId.toString())
            }
        )
    }

    suspend fun updateCategory(category: Category) = updateCategory(category.id)
}
