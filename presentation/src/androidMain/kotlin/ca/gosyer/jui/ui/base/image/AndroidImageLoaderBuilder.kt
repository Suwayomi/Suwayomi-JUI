/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.base.image

import android.os.Build
import ca.gosyer.jui.domain.server.Http
import ca.gosyer.jui.uicore.vm.ContextWrapper
import com.seiko.imageloader.cache.disk.DiskCacheBuilder
import com.seiko.imageloader.cache.memory.MemoryCacheBuilder
import com.seiko.imageloader.component.ComponentRegistryBuilder
import com.seiko.imageloader.component.setupDefaultComponents
import com.seiko.imageloader.option.Options
import okio.Path.Companion.toOkioPath

actual val imageConfig: Options.ImageConfig = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
    Options.ImageConfig.ARGB_8888
} else {
    Options.ImageConfig.HARDWARE
}

actual fun ComponentRegistryBuilder.register(contextWrapper: ContextWrapper, http: Http) {
    setupDefaultComponents(contextWrapper, httpClient = { http })
}

actual fun DiskCacheBuilder.configure(contextWrapper: ContextWrapper, cacheDir: String) {
    directory(contextWrapper.cacheDir.toOkioPath() / cacheDir)
    maxSizeBytes(1024 * 1024 * 150) // 150 MB
}

actual fun MemoryCacheBuilder.configure(contextWrapper: ContextWrapper) {
}
