/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.uicore.components

import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.HoverInteraction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.onClick
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

actual fun Modifier.buttonModifier(
    onClick: () -> Unit,
    onHintClick: () -> Unit,
): Modifier =
    composed {
        val interactionSource = remember { MutableInteractionSource() }
        LaunchedEffect(interactionSource) {
            launch {
                interactionSource.interactions
                    .mapLatest {
                        if (it !is HoverInteraction.Enter) return@mapLatest
                        delay(2.seconds)
                        onHintClick()
                    }
                    .collect()
            }
        }
        interactionSource.interactions
        onClick(onClick = onClick)
            .hoverable(interactionSource)
    }
