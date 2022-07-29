/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.data.source

import ca.gosyer.jui.core.lang.IO
import ca.gosyer.jui.data.base.BaseRepository
import ca.gosyer.jui.domain.server.Http
import ca.gosyer.jui.domain.server.model.requests.getFilterListQuery
import ca.gosyer.jui.domain.server.model.requests.getSourceSettingsQuery
import ca.gosyer.jui.domain.server.model.requests.setFilterRequest
import ca.gosyer.jui.domain.server.model.requests.sourceInfoQuery
import ca.gosyer.jui.domain.server.model.requests.sourceLatestQuery
import ca.gosyer.jui.domain.server.model.requests.sourceListQuery
import ca.gosyer.jui.domain.server.model.requests.sourcePopularQuery
import ca.gosyer.jui.domain.server.model.requests.sourceSearchQuery
import ca.gosyer.jui.domain.server.model.requests.updateSourceSettingQuery
import ca.gosyer.jui.domain.server.service.ServerPreferences
import ca.gosyer.jui.domain.source.model.MangaPage
import ca.gosyer.jui.domain.source.model.Source
import ca.gosyer.jui.domain.source.model.sourcefilters.SourceFilter
import ca.gosyer.jui.domain.source.model.sourcefilters.SourceFilterChange
import ca.gosyer.jui.domain.source.model.sourcepreference.SourcePreference
import ca.gosyer.jui.domain.source.model.sourcepreference.SourcePreferenceChange
import ca.gosyer.jui.domain.source.service.SourceRepository
import io.ktor.client.call.body
import io.ktor.client.plugins.expectSuccess
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.path
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import me.tatarka.inject.annotations.Inject

class SourceRepositoryImpl @Inject constructor(
    client: Http,
    serverPreferences: ServerPreferences
) : BaseRepository(client, serverPreferences), SourceRepository {

    override fun getSourceList() = flow {
        val response = client.get(
            buildUrl { path(sourceListQuery()) }
        ) {
            expectSuccess = true
        }.body<List<Source>>()
        emit(response)
    }.flowOn(Dispatchers.IO)

    override fun getSourceInfo(sourceId: Long) = flow {
        val response = client.get(
            buildUrl { path(sourceInfoQuery(sourceId)) }
        ) {
            expectSuccess = true
        }.body<Source>()
        emit(response)
    }.flowOn(Dispatchers.IO)

    override fun getPopularManga(sourceId: Long, pageNum: Int) = flow {
        val response = client.get(
            buildUrl { path(sourcePopularQuery(sourceId, pageNum)) }
        ) {
            expectSuccess = true
        }.body<MangaPage>()
        emit(response)
    }.flowOn(Dispatchers.IO)

    override fun getLatestManga(sourceId: Long, pageNum: Int) = flow {
        val response = client.get(
            buildUrl { path(sourceLatestQuery(sourceId, pageNum)) }
        ) {
            expectSuccess = true
        }.body<MangaPage>()
        emit(response)
    }.flowOn(Dispatchers.IO)

    override fun getSearchResults(sourceId: Long, searchTerm: String, pageNum: Int) = flow {
        val response = client.get(
            buildUrl {
                path(sourceSearchQuery(sourceId))
                parameter("pageNum", pageNum)
                if (searchTerm.isNotBlank()) {
                    parameter("searchTerm", searchTerm)
                }
            }
        ) {
            expectSuccess = true
        }.body<MangaPage>()
        emit(response)
    }.flowOn(Dispatchers.IO)

    override fun getFilterList(sourceId: Long, reset: Boolean) = flow {
        val response = client.get(
            buildUrl {
                path(getFilterListQuery(sourceId))
                if (reset) {
                    parameter("reset", true)
                }
            }
        ) {
            expectSuccess = true
        }.body<List<SourceFilter>>()
        emit(response)
    }.flowOn(Dispatchers.IO)

    override fun setFilter(sourceId: Long, sourceFilter: SourceFilterChange) = flow {
        val response = client.post(
            buildUrl { path(setFilterRequest(sourceId)) }
        ) {
            contentType(ContentType.Application.Json)
            setBody(sourceFilter)
            expectSuccess = true
        }
        emit(response)
    }.flowOn(Dispatchers.IO)

    override fun setFilter(sourceId: Long, position: Int, value: Any) = setFilter(
        sourceId,
        SourceFilterChange(position, value)
    )

    override fun setFilter(sourceId: Long, parentPosition: Int, childPosition: Int, value: Any) = setFilter(
        sourceId,
        SourceFilterChange(
            parentPosition,
            Json.encodeToString(SourceFilterChange(childPosition, value))
        )
    )

    override fun getSourceSettings(sourceId: Long) = flow {
        val response = client.get(
            buildUrl { path(getSourceSettingsQuery(sourceId)) }
        ) {
            expectSuccess = true
        }.body<List<SourcePreference>>()
        emit(response)
    }.flowOn(Dispatchers.IO)

    override fun setSourceSetting(sourceId: Long, sourcePreference: SourcePreferenceChange) = flow {
        val response = client.post(
            buildUrl { path(updateSourceSettingQuery(sourceId)) }
        ) {
            contentType(ContentType.Application.Json)
            setBody(sourcePreference)
            expectSuccess = true
        }
        emit(response)
    }.flowOn(Dispatchers.IO)

    override fun setSourceSetting(sourceId: Long, position: Int, value: Any) = setSourceSetting(
        sourceId,
        SourcePreferenceChange(position, value)
    )
}
