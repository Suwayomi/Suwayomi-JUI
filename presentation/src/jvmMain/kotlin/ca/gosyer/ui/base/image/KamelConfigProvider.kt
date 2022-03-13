/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.base.image

import ca.gosyer.data.models.Extension
import ca.gosyer.data.models.Manga
import ca.gosyer.data.models.Source
import ca.gosyer.data.server.Http
import ca.gosyer.data.server.ServerPreferences
import io.kamel.core.DataSource
import io.kamel.core.Resource
import io.kamel.core.config.DefaultCacheSize
import io.kamel.core.config.KamelConfig
import io.kamel.core.config.KamelConfigBuilder
import io.kamel.core.config.ResourceConfig
import io.kamel.core.config.fileFetcher
import io.kamel.core.config.stringMapper
import io.kamel.core.config.uriMapper
import io.kamel.core.config.urlMapper
import io.kamel.core.fetcher.Fetcher
import io.kamel.core.mapper.Mapper
import io.kamel.image.config.imageBitmapDecoder
import io.ktor.client.HttpClient
import io.ktor.client.call.receive
import io.ktor.client.features.onDownload
import io.ktor.client.request.request
import io.ktor.client.request.takeFrom
import io.ktor.client.request.url
import io.ktor.client.statement.HttpResponse
import io.ktor.http.Url
import io.ktor.utils.io.ByteReadChannel
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.channelFlow
import me.tatarka.inject.annotations.Inject

class KamelConfigProvider @Inject constructor(
    private val http: Http,
    serverPreferences: ServerPreferences
) {
    @OptIn(DelicateCoroutinesApi::class)
    val serverUrl = serverPreferences.serverUrl().stateIn(GlobalScope)

    fun get(resourcesFetcher: KamelConfigBuilder.() -> Unit): KamelConfig {
        return KamelConfig {
            // Default config
            imageBitmapCacheSize = DefaultCacheSize
            imageVectorCacheSize = DefaultCacheSize
            imageBitmapDecoder()
            stringMapper()
            urlMapper()
            uriMapper()
            fileFetcher()

            // JUI config
            fetcher(HttpFetcher(http))
            resourcesFetcher()
            mapper(MangaCoverMapper(serverUrl))
            mapper(ExtensionIconMapper(serverUrl))
            mapper(SourceIconMapper(serverUrl))
        }
    }

    class MangaCoverMapper(private val serverUrlStateFlow: StateFlow<Url>) : Mapper<Manga, Url> {
        override fun map(input: Manga): Url {
            return Url(serverUrlStateFlow.value.toString() + input.thumbnailUrl)
        }
    }

    class ExtensionIconMapper(private val serverUrlStateFlow: StateFlow<Url>) : Mapper<Extension, Url> {
        override fun map(input: Extension): Url {
            return Url(serverUrlStateFlow.value.toString() + input.iconUrl)
        }
    }

    class SourceIconMapper(private val serverUrlStateFlow: StateFlow<Url>) : Mapper<Source, Url> {
        override fun map(input: Source): Url {
            return Url(serverUrlStateFlow.value.toString() + input.iconUrl)
        }
    }

    private class HttpFetcher(private val client: HttpClient) : Fetcher<Url> {

        override val source: DataSource = DataSource.Network

        override val Url.isSupported: Boolean
            get() = protocol.name == "https" || protocol.name == "http"

        @OptIn(ExperimentalCoroutinesApi::class)
        override fun fetch(
            data: Url,
            resourceConfig: ResourceConfig
        ): Flow<Resource<ByteReadChannel>> = channelFlow {
            val response = client.request<HttpResponse> {
                onDownload { bytesSentTotal, contentLength ->
                    val progress = (bytesSentTotal.toFloat() / contentLength).coerceAtMost(1.0F)
                    send(Resource.Loading(progress))
                }
                takeFrom(resourceConfig.requestData)
                url(data)
            }
            val bytes = response.receive<ByteReadChannel>()
            send(Resource.Success(bytes))
        }

    }
}
