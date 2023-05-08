/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.uicore.image

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BrokenImage
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.drawscope.DrawScope.Companion.DefaultFilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import ca.gosyer.jui.uicore.components.LoadingScreen
import com.seiko.imageloader.ImageRequestState
import com.seiko.imageloader.model.ImageRequest
import com.seiko.imageloader.rememberAsyncImagePainter
import org.lighthousegames.logging.logging

private val log = logging()

private enum class ImageLoaderImageState {
    Loading,
    Success,
    Failure,
}

@Composable
fun ImageLoaderImage(
    data: Any,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    errorModifier: Modifier = modifier,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null,
    filterQuality: FilterQuality = DefaultFilterQuality,
    onLoading: (@Composable BoxScope.(Float) -> Unit)? = {
        LoadingScreen(progress = it.coerceIn(0.0F, 1.0F), modifier = modifier then Modifier.fillMaxSize())
    },
    onFailure: (@Composable BoxScope.(Throwable) -> Unit)? = {
        LaunchedEffect(it) {
            log.warn(it) { "Error loading image" }
        }
        Box(
            modifier = errorModifier then Modifier.fillMaxSize()
                .background(Color(0x1F888888)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Rounded.BrokenImage,
                contentDescription = null,
                tint = Color(0x1F888888),
                modifier = Modifier.size(24.dp),
            )
        }
    },
    contentAlignment: Alignment = Alignment.Center,
    animationSpec: FiniteAnimationSpec<Float>? = tween(),
) {
    key(data) {
        val request = remember { ImageRequest(data) }
        val painter = rememberAsyncImagePainter(
            request,
            contentScale = contentScale,
            filterQuality = filterQuality,
        )

        val progress = remember { mutableStateOf(-1F) }
        val error = remember { mutableStateOf<Throwable?>(null) }
        val state by derivedStateOf {
            when (val state = painter.requestState) {
                is ImageRequestState.Failure -> {
                    progress.value = 0.0F
                    error.value = state.error
                    ImageLoaderImageState.Failure
                }
                is ImageRequestState.Loading -> {
                    progress.value = 0.0F
                    ImageLoaderImageState.Loading
                }
                ImageRequestState.Success -> {
                    progress.value = 1.0F
                    ImageLoaderImageState.Success
                }
            }
        }
        if (animationSpec != null) {
            Crossfade(state, animationSpec = animationSpec, modifier = modifier) {
                Box(Modifier.fillMaxSize(), contentAlignment) {
                    when (it) {
                        ImageLoaderImageState.Loading -> if (onLoading != null) {
                            onLoading(progress.value)
                        }
                        ImageLoaderImageState.Success -> Image(
                            painter = painter,
                            contentDescription = contentDescription,
                            modifier = Modifier.fillMaxSize(),
                            alignment = alignment,
                            contentScale = contentScale,
                            alpha = alpha,
                            colorFilter = colorFilter,
                        )
                        ImageLoaderImageState.Failure -> {
                            if (onFailure != null) {
                                onFailure(error.value ?: return@Crossfade)
                            }
                        }
                    }
                }
            }
        } else {
            Box(modifier, contentAlignment) {
                Image(
                    painter = painter,
                    contentDescription = contentDescription,
                    modifier = Modifier.fillMaxSize(),
                    alignment = alignment,
                    contentScale = contentScale,
                    alpha = alpha,
                    colorFilter = colorFilter,
                )
            }
        }
    }
}
