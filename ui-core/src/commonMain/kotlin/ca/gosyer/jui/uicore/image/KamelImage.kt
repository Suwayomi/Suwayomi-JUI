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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import ca.gosyer.jui.uicore.components.LoadingScreen
import io.kamel.core.Resource
import org.lighthousegames.logging.logging
import io.kamel.image.KamelImage as BaseKamelImage

private val log = logging()

private enum class KamelImageState {
    Loading,
    Success,
    Failure,
}

@Composable
fun KamelImage(
    resource: Resource<Painter>,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    errorModifier: Modifier = modifier,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null,
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
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Rounded.BrokenImage,
                contentDescription = null,
                tint = Color(0x1F888888),
                modifier = Modifier.size(24.dp)
            )
        }
    },
    contentAlignment: Alignment = Alignment.Center,
    animationSpec: FiniteAnimationSpec<Float>? = tween()
) {
    if (animationSpec != null) {
        val progress = remember { mutableStateOf(-1F) }
        val image = remember { mutableStateOf<Painter?>(null) }
        val error = remember { mutableStateOf<Throwable?>(null) }
        val state by derivedStateOf {
            when (resource) {
                is Resource.Failure -> {
                    progress.value = -1F
                    error.value = resource.exception
                    KamelImageState.Failure
                }
                is Resource.Loading -> {
                    progress.value = resource.progress
                    KamelImageState.Loading
                }
                is Resource.Success -> {
                    progress.value = 1.0F
                    image.value = resource.value
                    KamelImageState.Success
                }
            }
        }
        Crossfade(state, animationSpec = animationSpec, modifier = modifier) {
            Box(Modifier.fillMaxSize(), contentAlignment) {
                when (it) {
                    KamelImageState.Loading -> if (onLoading != null) {
                        onLoading(progress.value)
                    }
                    KamelImageState.Success -> Box {
                        val value = image.value
                        if (value != null) {
                            Image(
                                value,
                                contentDescription,
                                modifier,
                                alignment,
                                contentScale,
                                alpha,
                                colorFilter
                            )
                        } else if (onLoading != null) {
                            onLoading(1.0F)
                        }
                    }
                    KamelImageState.Failure -> {
                        if (onFailure != null) {
                            onFailure(error.value ?: return@Crossfade)
                        }
                    }
                }
            }
        }
    } else {
        BaseKamelImage(
            resource,
            contentDescription,
            modifier,
            alignment,
            contentScale,
            alpha,
            colorFilter,
            onLoading,
            onFailure,
            contentAlignment,
            null
        )
    }
}
