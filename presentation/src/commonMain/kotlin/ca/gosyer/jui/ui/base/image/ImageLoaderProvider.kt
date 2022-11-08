/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.base.image

import ca.gosyer.jui.domain.extension.model.Extension
import ca.gosyer.jui.domain.manga.model.Manga
import ca.gosyer.jui.domain.server.Http
import ca.gosyer.jui.domain.server.service.ServerPreferences
import ca.gosyer.jui.domain.source.model.Source
import ca.gosyer.jui.ui.base.ImageCache
import ca.gosyer.jui.uicore.vm.ContextWrapper
import com.seiko.imageloader.ImageLoader
import com.seiko.imageloader.ImageLoaderBuilder
import com.seiko.imageloader.cache.disk.DiskCache
import com.seiko.imageloader.cache.memory.MemoryCache
import com.seiko.imageloader.component.keyer.Keyer
import com.seiko.imageloader.component.mapper.Mapper
import com.seiko.imageloader.request.Options
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import me.tatarka.inject.annotations.Inject

class ImageLoaderProvider @Inject constructor(
    private val http: Http,
    serverPreferences: ServerPreferences,
    private val context: ContextWrapper
) {
    @OptIn(DelicateCoroutinesApi::class)
    val serverUrl = serverPreferences.serverUrl().stateIn(GlobalScope)

    fun get(imageCache: ImageCache): ImageLoader {
        return imageLoaderBuilder(context).apply {
            httpClient { http }
            components {
                add(MangaCoverMapper())
                add(MangaCoverKeyer())
                add(ExtensionIconMapper())
                add(ExtensionIconKeyer())
                add(SourceIconMapper())
                add(SourceIconKeyer())
            }
            options(
                Options(config = imageConfig)
            )
            diskCache {
                imageCache
            }
            memoryCache {
                memoryCache(context)
            }
        }.build()
    }

    inner class MangaCoverMapper : Mapper<String> {
        override fun map(data: Any, options: Options): String? {
            if (data !is Manga) return null
            if (data.thumbnailUrl.isNullOrBlank()) return null
            return serverUrl.value.toString() + data.thumbnailUrl
        }
    }

    class MangaCoverKeyer : Keyer {
        override fun key(data: Any, options: Options): String? {
            if (data !is Manga) return null
            return "${data.sourceId}-${data.url}"
        }
    }

    inner class ExtensionIconMapper : Mapper<String> {
        override fun map(data: Any, options: Options): String? {
            if (data !is Extension) return null
            if (data.iconUrl.isBlank()) return null
            return serverUrl.value.toString() + data.iconUrl
        }
    }

    class ExtensionIconKeyer : Keyer {
        override fun key(data: Any, options: Options): String? {
            if (data !is Extension) return null
            return data.iconUrl
        }
    }

    inner class SourceIconMapper : Mapper<String> {
        override fun map(data: Any, options: Options): String? {
            if (data !is Source) return null
            if (data.iconUrl.isBlank()) return null
            return serverUrl.value.toString() + data.iconUrl
        }
    }

    class SourceIconKeyer : Keyer {
        override fun key(data: Any, options: Options): String? {
            if (data !is Source) return null
            return data.iconUrl
        }
    }
}

expect val imageConfig: Options.ImageConfig

expect fun imageLoaderBuilder(contextWrapper: ContextWrapper): ImageLoaderBuilder

expect fun diskCache(contextWrapper: ContextWrapper, cacheDir: String): DiskCache

expect fun memoryCache(contextWrapper: ContextWrapper): MemoryCache
