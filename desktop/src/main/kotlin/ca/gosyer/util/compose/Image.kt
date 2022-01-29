/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.util.compose

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import ca.gosyer.data.server.Http
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.jvm.javaio.copyTo
import org.jetbrains.skia.Image
import java.io.ByteArrayOutputStream
import java.nio.file.Path
import kotlin.io.path.readBytes

fun imageFromFile(file: Path): ImageBitmap {
    return Image.makeFromEncoded(file.readBytes()).toComposeImageBitmap()
}

suspend fun imageFromUrl(client: Http, url: String, block: HttpRequestBuilder.() -> Unit): ImageBitmap {
    return client.get<ByteReadChannel>(url, block).toImageBitmap()
}

suspend fun ByteReadChannel.toImageBitmap(): ImageBitmap {
    val bytes = ByteArrayOutputStream().use {
        this.copyTo(it)
        it.toByteArray()
    }
    return Image.makeFromEncoded(bytes).toComposeImageBitmap()
}
