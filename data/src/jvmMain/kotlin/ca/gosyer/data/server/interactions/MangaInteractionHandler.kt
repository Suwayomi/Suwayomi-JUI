/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.data.server.interactions

import ca.gosyer.data.models.Manga
import ca.gosyer.data.server.Http
import ca.gosyer.data.server.ServerPreferences
import ca.gosyer.data.server.requests.mangaQuery
import ca.gosyer.data.server.requests.mangaThumbnailQuery
import ca.gosyer.data.server.requests.updateMangaMetaRequest
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpMethod
import io.ktor.http.Parameters
import io.ktor.utils.io.ByteReadChannel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import me.tatarka.inject.annotations.Inject

class MangaInteractionHandler @Inject constructor(
    client: Http,
    serverPreferences: ServerPreferences
) : BaseInteractionHandler(client, serverPreferences) {

    fun getManga(mangaId: Long, refresh: Boolean = false) = flow {
        val response = client.get<Manga>(
            serverUrl + mangaQuery(mangaId)
        ) {
            url {
                if (refresh) {
                    parameter("onlineFetch", true)
                }
            }
        }
        emit(response)
    }.flowOn(Dispatchers.IO)

    fun getManga(manga: Manga, refresh: Boolean = false) = getManga(manga.id, refresh)

    fun getMangaThumbnail(mangaId: Long, block: HttpRequestBuilder.() -> Unit) = flow {
        val response = client.get<ByteReadChannel>(
            serverUrl + mangaThumbnailQuery(mangaId),
            block
        )
        emit(response)
    }.flowOn(Dispatchers.IO)

    fun updateMangaMeta(mangaId: Long, key: String, value: String) = flow {
        val response = client.submitForm<HttpResponse>(
            serverUrl + updateMangaMetaRequest(mangaId),
            formParameters = Parameters.build {
                append("key", key)
                append("value", value)
            }
        ) {
            method = HttpMethod.Patch
        }
        emit(response)
    }.flowOn(Dispatchers.IO)

    fun updateMangaMeta(manga: Manga, key: String, value: String) = updateMangaMeta(manga.id, key, value)
}
