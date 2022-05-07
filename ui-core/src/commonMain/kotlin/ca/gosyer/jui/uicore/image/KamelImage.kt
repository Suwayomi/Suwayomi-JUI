/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.uicore.image

import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BrokenImage
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
    onLoading: @Composable (Float) -> Unit = {
        LoadingScreen(progress = it, modifier = modifier then Modifier.fillMaxSize())
    },
    onFailure: @Composable (Throwable) -> Unit = {
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
    crossfade: Boolean = true,
    animationSpec: FiniteAnimationSpec<Float> = tween()
) {
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
        crossfade,
        animationSpec
    )
}
