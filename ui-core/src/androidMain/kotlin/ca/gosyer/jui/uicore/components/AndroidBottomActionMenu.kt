/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.uicore.components

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material.ripple
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed

actual fun Modifier.buttonModifier(
    onClick: () -> Unit,
    onHintClick: () -> Unit,
): Modifier =
    composed {
        combinedClickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = ripple(bounded = false),
            onLongClick = onHintClick,
            onClick = onClick,
        )
    }
