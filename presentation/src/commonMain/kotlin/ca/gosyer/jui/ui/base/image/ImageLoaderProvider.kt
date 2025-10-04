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
import com.seiko.imageloader.Bitmap
import com.seiko.imageloader.ImageLoader
import com.seiko.imageloader.cache.disk.DiskCacheBuilder
import com.seiko.imageloader.cache.memory.MemoryCacheBuilder
import com.seiko.imageloader.cache.memory.MemoryKey
import com.seiko.imageloader.component.ComponentRegistryBuilder
import com.seiko.imageloader.component.fetcher.MokoResourceFetcher
import com.seiko.imageloader.component.keyer.Keyer
import com.seiko.imageloader.component.mapper.Mapper
import com.seiko.imageloader.intercept.bitmapMemoryCacheConfig
import com.seiko.imageloader.option.Options
import com.seiko.imageloader.option.OptionsBuilder
import io.ktor.http.Url
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import me.tatarka.inject.annotations.Inject

@Inject
class ImageLoaderProvider(
    private val http: Http,
    serverPreferences: ServerPreferences,
    private val context: ContextWrapper,
) {
    @OptIn(DelicateCoroutinesApi::class)
    val serverUrl = serverPreferences.serverUrl().stateIn(GlobalScope)

    fun get(imageCache: ImageCache): ImageLoader =
        ImageLoader {
            components {
                register(context, http)
                add(MokoResourceFetcher.Factory())
                add(MangaCoverMapper())
                add(MangaCoverKeyer())
                add(ExtensionIconMapper())
                add(ExtensionIconKeyer())
                add(SourceIconMapper())
                add(SourceIconKeyer())
            }
            options {
                configure(context)
            }
            interceptor {
                diskCache { imageCache }
                bitmapMemoryCacheConfig { configure(context) }
            }
        }

    inner class MangaCoverMapper : Mapper<Url> {
        override fun map(
            data: Any,
            options: Options,
        ): Url? {
            if (data !is Manga) return null
            if (data.thumbnailUrl.isNullOrBlank()) return null
            return Url(serverUrl.value.toString() + data.thumbnailUrl)
        }
    }

    class MangaCoverKeyer : Keyer {
        override fun key(
            data: Any,
            options: Options,
            type: Keyer.Type,
        ): String? {
            if (data !is Manga) return null
            return "${data.sourceId}-${data.thumbnailUrl}-${data.thumbnailUrlLastFetched}"
        }
    }

    inner class ExtensionIconMapper : Mapper<Url> {
        override fun map(
            data: Any,
            options: Options,
        ): Url? {
            if (data !is Extension) return null
            if (data.iconUrl.isBlank()) return null
            return Url("${serverUrl.value}${data.iconUrl}")
        }
    }

    class ExtensionIconKeyer : Keyer {
        override fun key(
            data: Any,
            options: Options,
            type: Keyer.Type,
        ): String? {
            if (data !is Extension) return null
            return data.iconUrl
        }
    }

    inner class SourceIconMapper : Mapper<Url> {
        override fun map(
            data: Any,
            options: Options,
        ): Url? {
            if (data !is Source) return null
            if (data.iconUrl.isBlank()) return null
            return Url(serverUrl.value.toString() + data.iconUrl)
        }
    }

    class SourceIconKeyer : Keyer {
        override fun key(
            data: Any,
            options: Options,
            type: Keyer.Type,
        ): String? {
            if (data !is Source) return null
            return data.iconUrl
        }
    }
}

expect fun OptionsBuilder.configure(contextWrapper: ContextWrapper)

expect fun ComponentRegistryBuilder.register(
    contextWrapper: ContextWrapper,
    http: Http,
)

expect fun DiskCacheBuilder.configure(
    contextWrapper: ContextWrapper,
    cacheDir: String,
)

expect fun MemoryCacheBuilder<MemoryKey, Bitmap>.configure(contextWrapper: ContextWrapper)
