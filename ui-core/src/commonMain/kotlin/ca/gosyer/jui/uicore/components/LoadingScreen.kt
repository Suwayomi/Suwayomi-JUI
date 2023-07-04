/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.uicore.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ProgressIndicatorDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import ca.gosyer.jui.i18n.MR
import ca.gosyer.jui.uicore.resources.stringResource

@Composable
fun LoadingScreen(
    isLoading: Boolean = true,
    modifier: Modifier = Modifier.fillMaxSize(),
    // @FloatRange(from = 0.0, to = 1.0)
    progress: Float = 0.0F,
    errorMessage: String? = null,
    retryMessage: String = stringResource(MR.strings.action_retry),
    retry: (() -> Unit)? = null,
) {
    Crossfade(isLoading, modifier) {
        BoxWithConstraints(Modifier.fillMaxSize()) {
            if (it) {
                if (progress != 0.0F && !progress.isNaN()) {
                    val animatedProgress by animateFloatAsState(
                        targetValue = progress,
                        animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec,
                    )
                    CircularProgressIndicator(animatedProgress, Modifier.align(Alignment.Center))
                } else {
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                }
            } else {
                ErrorScreen(errorMessage, modifier, retryMessage, retry)
            }
        }
    }
}
