/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.data.server.interactions

import ca.gosyer.jui.core.lang.IO
import ca.gosyer.jui.data.models.Manga
import ca.gosyer.jui.data.server.Http
import ca.gosyer.jui.data.server.ServerPreferences
import ca.gosyer.jui.data.server.requests.mangaQuery
import ca.gosyer.jui.data.server.requests.mangaThumbnailQuery
import ca.gosyer.jui.data.server.requests.updateMangaMetaRequest
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

class MangaInteractionHandler @Inject constructor(
    client: Http,
    serverPreferences: ServerPreferences
) : BaseInteractionHandler(client, serverPreferences) {

    fun getManga(mangaId: Long, refresh: Boolean = false) = flow {
        val response = client.get(
            buildUrl {
                path(mangaQuery(mangaId))
                if (refresh) {
                    parameter("onlineFetch", true)
                }
            },
        ) {
            expectSuccess = true
        }.body<Manga>()
        emit(response)
    }.flowOn(Dispatchers.IO)

    fun getManga(manga: Manga, refresh: Boolean = false) = getManga(manga.id, refresh)

    fun getMangaThumbnail(mangaId: Long, block: HttpRequestBuilder.() -> Unit) = flow {
        val response = client.get(
            buildUrl { path(mangaThumbnailQuery(mangaId)) },
        ) {
            expectSuccess = true
            block()
        }.bodyAsChannel()
        emit(response)
    }.flowOn(Dispatchers.IO)

    fun updateMangaMeta(mangaId: Long, key: String, value: String) = flow {
        val response = client.submitForm(
            buildUrl { path(updateMangaMetaRequest(mangaId),) },
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

    fun updateMangaMeta(manga: Manga, key: String, value: String) = updateMangaMeta(manga.id, key, value)
}
