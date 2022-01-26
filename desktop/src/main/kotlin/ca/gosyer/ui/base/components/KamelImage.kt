/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.base.components

import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import io.kamel.core.Resource
import io.kamel.image.KamelImage as BaseKamelImage

@Composable
fun KamelImage(
    resource: Resource<Painter>,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null,
    onLoading: @Composable (Float) -> Unit = {
        LoadingScreen(progress = it, modifier = modifier then Modifier.fillMaxSize())
    },
    onFailure: @Composable (Throwable) -> Unit = {
        ErrorScreen(it.localizedMessage, modifier = modifier then Modifier.fillMaxSize())
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
