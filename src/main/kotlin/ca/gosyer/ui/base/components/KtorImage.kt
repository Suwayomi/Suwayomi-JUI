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
import ca.gosyer.util.system.kLogger
import io.ktor.client.features.onDownload
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

val logger = kLogger {}

@OptIn(DelicateCoroutinesApi::class)
@Composable
fun KtorImage(
    imageUrl: String,
    modifier: Modifier = Modifier.fillMaxSize(),
    loadingModifier: Modifier = modifier,
    contentDescription: String? = null,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null,
    client: Http = remember { AppScope.getInstance() }
) {
    BoxWithConstraints(modifier) {
        val drawable = remember { mutableStateOf<ImageBitmap?>(null) }
        val loading = remember { mutableStateOf(true) }
        val progress = remember { mutableStateOf(0.0F) }
        val error = remember { mutableStateOf<String?>(null) }
        DisposableEffect(imageUrl) {
            val handler = CoroutineExceptionHandler { _, throwable ->
                logger.error(throwable) { "Error loading image $imageUrl" }
                loading.value = false
                error.value = throwable.message
            }
            val job = GlobalScope.launch(handler) {
                if (drawable.value == null) {
                    drawable.value = imageFromUrl(client, imageUrl) {
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
                modifier = Modifier.fillMaxSize(),
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
