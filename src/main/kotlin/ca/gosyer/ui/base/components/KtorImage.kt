/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.base.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import ca.gosyer.common.di.AppScope
import ca.gosyer.data.server.Http
import ca.gosyer.util.compose.imageFromUrl
import ca.gosyer.util.lang.throwIfCancellation
import io.ktor.client.features.onDownload
import io.ktor.client.request.HttpRequestBuilder
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@OptIn(DelicateCoroutinesApi::class)
@Composable
fun KtorImage(
    imageUrl: String,
    imageModifier: Modifier = Modifier.fillMaxSize(),
    loadingModifier: Modifier = imageModifier,
    contentDescription: String? = null,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null,
    retries: Int = 3,
    client: Http = remember { AppScope.getInstance() }
) {
    BoxWithConstraints {
        val drawable: MutableState<ImageBitmap?> = remember { mutableStateOf(null) }
        val loading: MutableState<Boolean> = remember { mutableStateOf(true) }
        val progress: MutableState<Float> = remember { mutableStateOf(0.0F) }
        val error: MutableState<String?> = remember { mutableStateOf(null) }
        DisposableEffect(imageUrl) {
            val handler = CoroutineExceptionHandler { _, throwable ->
                loading.value = false
                error.value = throwable.message
            }
            val job = GlobalScope.launch(handler) {
                if (drawable.value == null) {
                    drawable.value = getImage(client, imageUrl, retries) {
                        onDownload { bytesSentTotal, contentLength ->
                            progress.value = (bytesSentTotal.toFloat() / contentLength).coerceAtMost(1.0F)
                        }
                    }
                }
                loading.value = false
            }

            onDispose {
                job.cancel()
                drawable.value = null
            }
        }

        val value = drawable.value
        if (value != null) {
            Image(
                value,
                modifier = imageModifier,
                contentDescription = contentDescription,
                alignment = alignment,
                contentScale = contentScale,
                alpha = alpha,
                colorFilter = colorFilter
            )
        } else {
            LoadingScreen(loading.value, loadingModifier, progress.value, error.value)
        }
    }
}

private suspend fun getImage(client: Http, imageUrl: String, retries: Int = 3, block: HttpRequestBuilder.() -> Unit): ImageBitmap {
    var attempt = 1
    var lastException: Exception
    do {
        try {
            return imageFromUrl(client, imageUrl, block)
        } catch (e: Exception) {
            e.throwIfCancellation()
            lastException = e
        }
        attempt++
    } while (attempt <= retries)
    throw lastException
}
