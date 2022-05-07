/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.util.compose

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import ca.gosyer.jui.data.server.Http
import io.ktor.client.call.body
import io.ktor.client.plugins.expectSuccess
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import okio.FileSystem
import okio.Path
import okio.buffer
import org.jetbrains.skia.Image

fun imageFromFile(file: Path): ImageBitmap {
    return Image.makeFromEncoded(FileSystem.SYSTEM.source(file).buffer().readByteArray())
        .toComposeImageBitmap()
}

suspend fun imageFromUrl(client: Http, url: String, block: HttpRequestBuilder.() -> Unit): ImageBitmap {
    return client.get(url) {
        expectSuccess = true
        block()
    }.toImageBitmap()
}

actual suspend fun HttpResponse.toImageBitmap(): ImageBitmap {
    return Image.makeFromEncoded(body<ByteArray>()).toComposeImageBitmap()
}
