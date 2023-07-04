/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.base.image

import ca.gosyer.jui.domain.server.Http
import ca.gosyer.jui.uicore.vm.ContextWrapper
import com.seiko.imageloader.cache.disk.DiskCacheBuilder
import com.seiko.imageloader.cache.memory.MemoryCacheBuilder
import com.seiko.imageloader.cache.memory.maxSizePercent
import com.seiko.imageloader.component.ComponentRegistryBuilder
import com.seiko.imageloader.component.setupDefaultComponents
import com.seiko.imageloader.option.OptionsBuilder
import okio.Path.Companion.toPath
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

actual fun OptionsBuilder.configure(contextWrapper: ContextWrapper) {
}

actual fun ComponentRegistryBuilder.register(
    contextWrapper: ContextWrapper,
    http: Http,
) {
    setupDefaultComponents(httpClient = { http })
}

actual fun DiskCacheBuilder.configure(
    contextWrapper: ContextWrapper,
    cacheDir: String,
) {
    directory(getCacheDir().toPath() / cacheDir)
    maxSizeBytes(1024 * 1024 * 150) // 150 MB
}

private fun getCacheDir(): String {
    return NSFileManager.defaultManager.URLForDirectory(
        NSCachesDirectory,
        NSUserDomainMask,
        null,
        true,
        null,
    )!!.path.orEmpty()
}

actual fun MemoryCacheBuilder.configure(contextWrapper: ContextWrapper) {
    maxSizePercent(0.25)
}
