/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.util.compose

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asComposeImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import org.jetbrains.skia.Image
import com.seiko.imageloader.Image as ImageLoaderImage

actual suspend fun HttpResponse.toImageBitmap(): ImageBitmap {
    return Image.makeFromEncoded(body<ByteArray>()).toComposeImageBitmap()
}

actual fun ImageLoaderImage.asImageBitmap(): ImageBitmap {
    return asComposeImageBitmap()
}
