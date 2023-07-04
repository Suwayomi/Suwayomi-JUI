/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.base

import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.compositionLocalOf
import ca.gosyer.jui.core.di.AppScope
import ca.gosyer.jui.core.io.SYSTEM
import ca.gosyer.jui.ui.ViewModelComponent
import ca.gosyer.jui.ui.base.image.ImageLoaderProvider
import ca.gosyer.jui.ui.base.image.configure
import ca.gosyer.jui.uicore.vm.ContextWrapper
import com.seiko.imageloader.ImageLoader
import com.seiko.imageloader.LocalImageLoader
import com.seiko.imageloader.cache.disk.DiskCache
import me.tatarka.inject.annotations.Provides
import okio.FileSystem

typealias ImageCache = DiskCache

typealias ChapterCache = DiskCache

interface UiComponent {
    val imageLoader: ImageLoader

    val contextWrapper: ContextWrapper

    val hooks: Array<ProvidedValue<out Any?>>

    val imageCache: ImageCache

    val chapterCache: ChapterCache

    @AppScope
    @Provides
    fun imageLoaderFactory(
        imageLoaderProvider: ImageLoaderProvider,
        imageCache: ImageCache,
    ): ImageLoader = imageLoaderProvider.get(imageCache)

    @AppScope
    @Provides
    fun imageCacheFactory(): ImageCache =
        DiskCache(FileSystem.SYSTEM) {
            configure(contextWrapper, "image_cache")
        }

    @AppScope
    @Provides
    fun chapterCacheFactory(): ChapterCache =
        DiskCache(FileSystem.SYSTEM) {
            configure(contextWrapper, "chapter_cache")
        }

    @Provides
    fun getHooks(viewModelComponent: ViewModelComponent) =
        arrayOf(
            LocalViewModels provides viewModelComponent,
            LocalImageLoader provides imageLoader,
        )
}

val LocalViewModels =
    compositionLocalOf<ViewModelComponent> { throw IllegalArgumentException("ViewModelComponent not found") }
