/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.backend.network.interactions

import ca.gosyer.backend.models.MangaPage
import ca.gosyer.backend.models.Source
import ca.gosyer.backend.network.requests.getFilterListQuery
import ca.gosyer.backend.network.requests.globalSearchQuery
import ca.gosyer.backend.network.requests.sourceInfoQuery
import ca.gosyer.backend.network.requests.sourceLatestQuery
import ca.gosyer.backend.network.requests.sourceListQuery
import ca.gosyer.backend.network.requests.sourcePopularQuery
import ca.gosyer.backend.network.requests.sourceSearchQuery
import io.ktor.client.HttpClient
import io.ktor.client.statement.HttpResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SourceInteractionHandler(private val client: HttpClient): BaseInteractionHandler() {

    suspend fun getSourceList() = withContext(Dispatchers.IO) {
        client.getRepeat<List<Source>>(
            serverUrl + sourceListQuery()
        )
    }

    suspend fun getSourceInfo(sourceId: Long) = withContext(Dispatchers.IO) {
        client.getRepeat<Source>(
            serverUrl + sourceInfoQuery(sourceId)
        )
    }

    suspend fun getSourceInfo(source: Source) = getSourceInfo(source.id)

    suspend fun getPopularManga(sourceId: Long, pageNum: Int) = withContext(Dispatchers.IO) {
        client.getRepeat<MangaPage>(
            serverUrl + sourcePopularQuery(sourceId, pageNum)
        )
    }

    suspend fun getPopularManga(source: Source, pageNum: Int) = getPopularManga(
        source.id, pageNum
    )

    suspend fun getLatestManga(sourceId: Long, pageNum: Int) = withContext(Dispatchers.IO) {
        client.getRepeat<MangaPage>(
            serverUrl + sourceLatestQuery(sourceId, pageNum)
        )
    }

    suspend fun getLatestManga(source: Source, pageNum: Int) = getLatestManga(
        source.id, pageNum
    )

    // TODO: 2021-03-14
    suspend fun getGlobalSearchResults(searchTerm: String) = withContext(Dispatchers.IO) {
        client.getRepeat<HttpResponse>(
            serverUrl + globalSearchQuery(searchTerm)
        )
    }

    suspend fun getSearchResults(sourceId: Long, searchTerm: String, pageNum: Int) = withContext(Dispatchers.IO) {
        client.getRepeat<MangaPage>(
            serverUrl + sourceSearchQuery(sourceId, searchTerm, pageNum)
        )
    }

    suspend fun getSearchResults(source: Source, searchTerm: String, pageNum: Int) = getSearchResults(
        source.id, searchTerm, pageNum
    )

    // TODO: 2021-03-14  
    suspend fun getFilterList(sourceId: Long) = withContext(Dispatchers.IO) {
        client.getRepeat<HttpResponse>(
            serverUrl + getFilterListQuery(sourceId)
        )
    }

    suspend fun getFilterList(source: Source) = getFilterList(source.id)
}