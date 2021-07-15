/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.data.server.interactions

import ca.gosyer.data.models.MangaPage
import ca.gosyer.data.models.Source
import ca.gosyer.data.server.Http
import ca.gosyer.data.server.ServerPreferences
import ca.gosyer.data.server.requests.getFilterListQuery
import ca.gosyer.data.server.requests.globalSearchQuery
import ca.gosyer.data.server.requests.sourceInfoQuery
import ca.gosyer.data.server.requests.sourceLatestQuery
import ca.gosyer.data.server.requests.sourceListQuery
import ca.gosyer.data.server.requests.sourcePopularQuery
import ca.gosyer.data.server.requests.sourceSearchQuery
import ca.gosyer.util.lang.withIOContext
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import javax.inject.Inject

class SourceInteractionHandler @Inject constructor(
    client: Http,
    serverPreferences: ServerPreferences
) : BaseInteractionHandler(client, serverPreferences) {

    suspend fun getSourceList() = withIOContext {
        client.get<List<Source>>(
            serverUrl + sourceListQuery()
        )
    }

    suspend fun getSourceInfo(sourceId: Long) = withIOContext {
        client.get<Source>(
            serverUrl + sourceInfoQuery(sourceId)
        )
    }

    suspend fun getSourceInfo(source: Source) = getSourceInfo(source.id)

    suspend fun getPopularManga(sourceId: Long, pageNum: Int) = withIOContext {
        client.get<MangaPage>(
            serverUrl + sourcePopularQuery(sourceId, pageNum)
        )
    }

    suspend fun getPopularManga(source: Source, pageNum: Int) = getPopularManga(
        source.id,
        pageNum
    )

    suspend fun getLatestManga(sourceId: Long, pageNum: Int) = withIOContext {
        client.get<MangaPage>(
            serverUrl + sourceLatestQuery(sourceId, pageNum)
        )
    }

    suspend fun getLatestManga(source: Source, pageNum: Int) = getLatestManga(
        source.id,
        pageNum
    )

    // TODO: 2021-03-14
    suspend fun getGlobalSearchResults(searchTerm: String) = withIOContext {
        client.get<HttpResponse>(
            serverUrl + globalSearchQuery(searchTerm)
        )
    }

    suspend fun getSearchResults(sourceId: Long, searchTerm: String, pageNum: Int) = withIOContext {
        client.get<MangaPage>(
            serverUrl + sourceSearchQuery(sourceId, searchTerm, pageNum)
        )
    }

    suspend fun getSearchResults(source: Source, searchTerm: String, pageNum: Int) = getSearchResults(
        source.id,
        searchTerm,
        pageNum
    )

    // TODO: 2021-03-14  
    suspend fun getFilterList(sourceId: Long) = withIOContext {
        client.get<HttpResponse>(
            serverUrl + getFilterListQuery(sourceId)
        )
    }

    suspend fun getFilterList(source: Source) = getFilterList(source.id)
}
