/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.data.manga

import ca.gosyer.jui.core.lang.IO
import ca.gosyer.jui.data.base.BaseRepository
import ca.gosyer.jui.domain.manga.model.Manga
import ca.gosyer.jui.domain.manga.service.MangaRepository
import ca.gosyer.jui.domain.server.Http
import ca.gosyer.jui.domain.server.model.requests.mangaQuery
import ca.gosyer.jui.domain.server.model.requests.mangaThumbnailQuery
import ca.gosyer.jui.domain.server.model.requests.updateMangaMetaRequest
import ca.gosyer.jui.domain.server.service.ServerPreferences
import io.ktor.client.call.body
import io.ktor.client.plugins.expectSuccess
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.HttpMethod
import io.ktor.http.Parameters
import io.ktor.http.path
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import me.tatarka.inject.annotations.Inject

class MangaRepositoryImpl @Inject constructor(
    client: Http,
    serverPreferences: ServerPreferences
) : BaseRepository(client, serverPreferences), MangaRepository {

    override fun getManga(mangaId: Long, refresh: Boolean) = flow {
        val response = client.get(
            buildUrl {
                path(mangaQuery(mangaId))
                if (refresh) {
                    parameter("onlineFetch", true)
                }
            }
        ) {
            expectSuccess = true
        }.body<Manga>()
        emit(response)
    }.flowOn(Dispatchers.IO)

    override fun getMangaThumbnail(mangaId: Long, block: HttpRequestBuilder.() -> Unit) = flow {
        val response = client.get(
            buildUrl { path(mangaThumbnailQuery(mangaId)) }
        ) {
            expectSuccess = true
            block()
        }.bodyAsChannel()
        emit(response)
    }.flowOn(Dispatchers.IO)

    override fun updateMangaMeta(mangaId: Long, key: String, value: String) = flow {
        val response = client.submitForm(
            buildUrl { path(updateMangaMetaRequest(mangaId)) },
            formParameters = Parameters.build {
                append("key", key)
                append("value", value)
            }
        ) {
            method = HttpMethod.Patch
            expectSuccess = true
        }
        emit(response)
    }.flowOn(Dispatchers.IO)
}
