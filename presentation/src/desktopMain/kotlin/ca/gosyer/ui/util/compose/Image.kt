/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.util.compose

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import ca.gosyer.data.server.Http
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.jvm.javaio.copyTo
import okio.FileSystem
import okio.Path
import okio.buffer
import org.jetbrains.skia.Image
import java.io.ByteArrayOutputStream

fun imageFromFile(file: Path): ImageBitmap {
    return Image.makeFromEncoded(FileSystem.SYSTEM.source(file).buffer().readByteArray())
        .toComposeImageBitmap()
}

suspend fun imageFromUrl(client: Http, url: String, block: HttpRequestBuilder.() -> Unit): ImageBitmap {
    return client.get<ByteReadChannel>(url, block).toImageBitmap()
}

actual suspend fun ByteReadChannel.toImageBitmap(): ImageBitmap {
    val bytes = ByteArrayOutputStream().use {
        this.copyTo(it)
        it.toByteArray()
    }
    return Image.makeFromEncoded(bytes).toComposeImageBitmap()
}
