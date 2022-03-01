/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.data.server.interactions

import ca.gosyer.data.models.MangaPage
import ca.gosyer.data.models.Source
import ca.gosyer.data.models.sourcefilters.SourceFilter
import ca.gosyer.data.models.sourcefilters.SourceFilterChange
import ca.gosyer.data.models.sourcepreference.SourcePreference
import ca.gosyer.data.models.sourcepreference.SourcePreferenceChange
import ca.gosyer.data.server.Http
import ca.gosyer.data.server.ServerPreferences
import ca.gosyer.data.server.requests.getFilterListQuery
import ca.gosyer.data.server.requests.getSourceSettingsQuery
import ca.gosyer.data.server.requests.globalSearchQuery
import ca.gosyer.data.server.requests.setFilterRequest
import ca.gosyer.data.server.requests.sourceInfoQuery
import ca.gosyer.data.server.requests.sourceLatestQuery
import ca.gosyer.data.server.requests.sourceListQuery
import ca.gosyer.data.server.requests.sourcePopularQuery
import ca.gosyer.data.server.requests.sourceSearchQuery
import ca.gosyer.data.server.requests.updateSourceSettingQuery
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import me.tatarka.inject.annotations.Inject

class SourceInteractionHandler @Inject constructor(
    client: Http,
    serverPreferences: ServerPreferences
) : BaseInteractionHandler(client, serverPreferences) {

    fun getSourceList() = flow {
        val response = client.get<List<Source>>(
            serverUrl + sourceListQuery()
        )
        emit(response)
    }.flowOn(Dispatchers.IO)

    fun getSourceInfo(sourceId: Long) = flow {
        val response = client.get<Source>(
            serverUrl + sourceInfoQuery(sourceId)
        )
        emit(response)
    }.flowOn(Dispatchers.IO)

    fun getSourceInfo(source: Source) = getSourceInfo(source.id)

    fun getPopularManga(sourceId: Long, pageNum: Int) = flow {
        val response = client.get<MangaPage>(
            serverUrl + sourcePopularQuery(sourceId, pageNum)
        )
        emit(response)
    }.flowOn(Dispatchers.IO)

    fun getPopularManga(source: Source, pageNum: Int) = getPopularManga(
        source.id,
        pageNum
    )

    fun getLatestManga(sourceId: Long, pageNum: Int) = flow {
        val response = client.get<MangaPage>(
            serverUrl + sourceLatestQuery(sourceId, pageNum)
        )
        emit(response)
    }.flowOn(Dispatchers.IO)

    fun getLatestManga(source: Source, pageNum: Int) = getLatestManga(
        source.id,
        pageNum
    )

    // TODO: 2021-03-14
    fun getGlobalSearchResults(searchTerm: String) = flow {
        val response = client.get<HttpResponse>(
            serverUrl + globalSearchQuery()
        ) {
            url {
                if (searchTerm.isNotBlank()) {
                    parameter("searchTerm", searchTerm)
                }
            }
        }
        emit(response)
    }.flowOn(Dispatchers.IO)

    fun getSearchResults(sourceId: Long, searchTerm: String, pageNum: Int) = flow {
        val response = client.get<MangaPage>(
            serverUrl + sourceSearchQuery(sourceId)
        ) {
            url {
                parameter("pageNum", pageNum)
                if (searchTerm.isNotBlank()) {
                    parameter("searchTerm", searchTerm)
                }
            }
        }
        emit(response)
    }.flowOn(Dispatchers.IO)

    fun getSearchResults(source: Source, searchTerm: String, pageNum: Int) = getSearchResults(
        source.id,
        searchTerm,
        pageNum
    )

    fun getFilterList(sourceId: Long, reset: Boolean = false) = flow {
        val response = client.get<List<SourceFilter>>(
            serverUrl + getFilterListQuery(sourceId)
        ) {
            url {
                if (reset) {
                    parameter("reset", true)
                }
            }
        }
        emit(response)
    }.flowOn(Dispatchers.IO)

    fun getFilterList(source: Source, reset: Boolean = false) = getFilterList(source.id, reset)

    fun setFilter(sourceId: Long, sourceFilter: SourceFilterChange) = flow {
        val response = client.post<HttpResponse>(
            serverUrl + setFilterRequest(sourceId)
        ) {
            contentType(ContentType.Application.Json)
            body = sourceFilter
        }
        emit(response)
    }.flowOn(Dispatchers.IO)

    fun setFilter(sourceId: Long, position: Int, value: Any) = setFilter(
        sourceId,
        SourceFilterChange(position, value)
    )

    fun setFilter(sourceId: Long, parentPosition: Int, childPosition: Int, value: Any) = setFilter(
        sourceId,
        SourceFilterChange(
            parentPosition,
            Json.encodeToString(SourceFilterChange(childPosition, value))
        )
    )

    fun getSourceSettings(sourceId: Long) = flow {
        val response = client.get<List<SourcePreference>>(
            serverUrl + getSourceSettingsQuery(sourceId)
        )
        emit(response)
    }.flowOn(Dispatchers.IO)

    fun getSourceSettings(source: Source) = getSourceSettings(source.id)

    fun setSourceSetting(sourceId: Long, sourcePreference: SourcePreferenceChange) = flow {
        val response = client.post<HttpResponse>(
            serverUrl + updateSourceSettingQuery(sourceId)
        ) {
            contentType(ContentType.Application.Json)
            body = sourcePreference
        }
        emit(response)
    }.flowOn(Dispatchers.IO)

    fun setSourceSetting(sourceId: Long, position: Int, value: Any) = setSourceSetting(
        sourceId,
        SourcePreferenceChange(position, value)
    )
}
