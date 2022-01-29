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
import ca.gosyer.ui.base.prefs.asStateIn
import io.kamel.core.config.DefaultCacheSize
import io.kamel.core.config.KamelConfig
import io.kamel.core.config.fileFetcher
import io.kamel.core.config.httpFetcher
import io.kamel.core.config.stringMapper
import io.kamel.core.config.uriMapper
import io.kamel.core.config.urlMapper
import io.kamel.core.mapper.Mapper
import io.kamel.image.config.imageBitmapDecoder
import io.kamel.image.config.resourcesFetcher
import io.ktor.http.Url
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import me.tatarka.inject.annotations.Inject

class KamelConfigProvider @Inject constructor(
    private val http: Http,
    serverPreferences: ServerPreferences
) {
    @OptIn(DelicateCoroutinesApi::class)
    val serverUrl = serverPreferences.serverUrl().asStateIn(GlobalScope)

    fun get(): KamelConfig {
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
            httpFetcher(http.engine) {
                install(http)
            }
            resourcesFetcher()
            val serverUrl = serverUrl.asStateFlow()
            mapper(MangaCoverMapper(serverUrl))
            mapper(ExtensionIconMapper(serverUrl))
            mapper(SourceIconMapper(serverUrl))
        }
    }

    class MangaCoverMapper(private val serverUrlStateFlow: StateFlow<String>) : Mapper<Manga, Url> {
        override fun map(input: Manga): Url {
            return Url(serverUrlStateFlow.value + input.thumbnailUrl)
        }
    }

    class ExtensionIconMapper(private val serverUrlStateFlow: StateFlow<String>) : Mapper<Extension, Url> {
        override fun map(input: Extension): Url {
            return Url(serverUrlStateFlow.value + input.iconUrl)
        }
    }

    class SourceIconMapper(private val serverUrlStateFlow: StateFlow<String>) : Mapper<Source, Url> {
        override fun map(input: Source): Url {
            return Url(serverUrlStateFlow.value + input.iconUrl)
        }
    }
}
