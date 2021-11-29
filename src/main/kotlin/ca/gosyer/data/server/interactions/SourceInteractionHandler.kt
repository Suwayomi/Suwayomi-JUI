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
import ca.gosyer.util.lang.withIOContext
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
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
            serverUrl + globalSearchQuery()
        ) {
            url {
                if (searchTerm.isNotBlank()) {
                    parameter("searchTerm", searchTerm)
                }
            }
        }
    }

    suspend fun getSearchResults(sourceId: Long, searchTerm: String, pageNum: Int) = withIOContext {
        client.get<MangaPage>(
            serverUrl + sourceSearchQuery(sourceId)
        ) {
            url {
                parameter("pageNum", pageNum)
                if (searchTerm.isNotBlank()) {
                    parameter("searchTerm", searchTerm)
                }
            }
        }
    }

    suspend fun getSearchResults(source: Source, searchTerm: String, pageNum: Int) = getSearchResults(
        source.id,
        searchTerm,
        pageNum
    )

    suspend fun getFilterList(sourceId: Long, reset: Boolean = false) = withIOContext {
        client.get<List<SourceFilter>>(
            serverUrl + getFilterListQuery(sourceId)
        ) {
            url {
                if (reset) {
                    parameter("reset", true)
                }
            }
        }
    }

    suspend fun getFilterList(source: Source, reset: Boolean = false) = getFilterList(source.id, reset)

    suspend fun setFilter(sourceId: Long, sourceFilter: SourceFilterChange) = withIOContext {
        client.post<HttpResponse>(
            serverUrl + setFilterRequest(sourceId)
        ) {
            contentType(ContentType.Application.Json)
            body = sourceFilter
        }
    }

    suspend fun setFilter(sourceId: Long, position: Int, value: Any) = setFilter(
        sourceId,
        SourceFilterChange(position, value)
    )

    suspend fun setFilter(sourceId: Long, parentPosition: Int, childPosition: Int, value: Any) = setFilter(
        sourceId,
        SourceFilterChange(
            parentPosition,
            Json.encodeToString(SourceFilterChange(childPosition, value))
        )
    )

    suspend fun getSourceSettings(sourceId: Long) = withIOContext {
        client.get<List<SourcePreference>>(
            serverUrl + getSourceSettingsQuery(sourceId)
        )
    }

    suspend fun getSourceSettings(source: Source) = getSourceSettings(source.id)

    suspend fun setSourceSetting(sourceId: Long, sourcePreference: SourcePreferenceChange) = withIOContext {
        client.post<HttpResponse>(
            serverUrl + updateSourceSettingQuery(sourceId)
        ) {
            contentType(ContentType.Application.Json)
            body = sourcePreference
        }
    }

    suspend fun setSourceSetting(sourceId: Long, position: Int, value: Any) = setSourceSetting(
        sourceId,
        SourcePreferenceChange(position, value)
    )
}
