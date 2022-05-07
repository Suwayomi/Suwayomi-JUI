/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.data.server.interactions

import ca.gosyer.jui.core.lang.IO
import ca.gosyer.jui.data.models.MangaPage
import ca.gosyer.jui.data.models.Source
import ca.gosyer.jui.data.models.sourcefilters.SourceFilter
import ca.gosyer.jui.data.models.sourcefilters.SourceFilterChange
import ca.gosyer.jui.data.models.sourcepreference.SourcePreference
import ca.gosyer.jui.data.models.sourcepreference.SourcePreferenceChange
import ca.gosyer.jui.data.server.Http
import ca.gosyer.jui.data.server.ServerPreferences
import ca.gosyer.jui.data.server.requests.getFilterListQuery
import ca.gosyer.jui.data.server.requests.getSourceSettingsQuery
import ca.gosyer.jui.data.server.requests.globalSearchQuery
import ca.gosyer.jui.data.server.requests.setFilterRequest
import ca.gosyer.jui.data.server.requests.sourceInfoQuery
import ca.gosyer.jui.data.server.requests.sourceLatestQuery
import ca.gosyer.jui.data.server.requests.sourceListQuery
import ca.gosyer.jui.data.server.requests.sourcePopularQuery
import ca.gosyer.jui.data.server.requests.sourceSearchQuery
import ca.gosyer.jui.data.server.requests.updateSourceSettingQuery
import io.ktor.client.call.body
import io.ktor.client.plugins.expectSuccess
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
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
        val response = client.get(
            serverUrl + sourceListQuery()
        ) {
            expectSuccess = true
        }.body<List<Source>>()
        emit(response)
    }.flowOn(Dispatchers.IO)

    fun getSourceInfo(sourceId: Long) = flow {
        val response = client.get(
            serverUrl + sourceInfoQuery(sourceId)
        ) {
            expectSuccess = true
        }.body<Source>()
        emit(response)
    }.flowOn(Dispatchers.IO)

    fun getSourceInfo(source: Source) = getSourceInfo(source.id)

    fun getPopularManga(sourceId: Long, pageNum: Int) = flow {
        val response = client.get(
            serverUrl + sourcePopularQuery(sourceId, pageNum)
        ) {
            expectSuccess = true
        }.body<MangaPage>()
        emit(response)
    }.flowOn(Dispatchers.IO)

    fun getPopularManga(source: Source, pageNum: Int) = getPopularManga(
        source.id,
        pageNum
    )

    fun getLatestManga(sourceId: Long, pageNum: Int) = flow {
        val response = client.get(
            serverUrl + sourceLatestQuery(sourceId, pageNum)
        ) {
            expectSuccess = true
        }.body<MangaPage>()
        emit(response)
    }.flowOn(Dispatchers.IO)

    fun getLatestManga(source: Source, pageNum: Int) = getLatestManga(
        source.id,
        pageNum
    )

    // TODO: 2021-03-14
    fun getGlobalSearchResults(searchTerm: String) = flow {
        val response = client.get(
            serverUrl + globalSearchQuery()
        ) {
            url {
                if (searchTerm.isNotBlank()) {
                    parameter("searchTerm", searchTerm)
                }
            }
            expectSuccess = true
        }
        emit(response)
    }.flowOn(Dispatchers.IO)

    fun getSearchResults(sourceId: Long, searchTerm: String, pageNum: Int) = flow {
        val response = client.get(
            serverUrl + sourceSearchQuery(sourceId)
        ) {
            url {
                parameter("pageNum", pageNum)
                if (searchTerm.isNotBlank()) {
                    parameter("searchTerm", searchTerm)
                }
            }
            expectSuccess = true
        }.body<MangaPage>()
        emit(response)
    }.flowOn(Dispatchers.IO)

    fun getSearchResults(source: Source, searchTerm: String, pageNum: Int) = getSearchResults(
        source.id,
        searchTerm,
        pageNum
    )

    fun getFilterList(sourceId: Long, reset: Boolean = false) = flow {
        val response = client.get(
            serverUrl + getFilterListQuery(sourceId)
        ) {
            url {
                if (reset) {
                    parameter("reset", true)
                }
            }
            expectSuccess = true
        }.body<List<SourceFilter>>()
        emit(response)
    }.flowOn(Dispatchers.IO)

    fun getFilterList(source: Source, reset: Boolean = false) = getFilterList(source.id, reset)

    fun setFilter(sourceId: Long, sourceFilter: SourceFilterChange) = flow {
        val response = client.post(
            serverUrl + setFilterRequest(sourceId)
        ) {
            contentType(ContentType.Application.Json)
            setBody(sourceFilter)
            expectSuccess = true
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
        val response = client.get(
            serverUrl + getSourceSettingsQuery(sourceId)
        ) {
            expectSuccess = true
        }.body<List<SourcePreference>>()
        emit(response)
    }.flowOn(Dispatchers.IO)

    fun getSourceSettings(source: Source) = getSourceSettings(source.id)

    fun setSourceSetting(sourceId: Long, sourcePreference: SourcePreferenceChange) = flow {
        val response = client.post(
            serverUrl + updateSourceSettingQuery(sourceId)
        ) {
            contentType(ContentType.Application.Json)
            setBody(sourcePreference)
            expectSuccess = true
        }
        emit(response)
    }.flowOn(Dispatchers.IO)

    fun setSourceSetting(sourceId: Long, position: Int, value: Any) = setSourceSetting(
        sourceId,
        SourcePreferenceChange(position, value)
    )
}
