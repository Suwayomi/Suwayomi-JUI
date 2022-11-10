/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.uicore.components

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha

fun Modifier.selectedBackground(isSelected: Boolean): Modifier = composed {
    if (isSelected) {
        val alpha = if (isSystemInDarkTheme()) 0.08f else 0.22f
        background(MaterialTheme.colors.secondary.copy(alpha = alpha))
    } else {
        this
    }
}

fun Modifier.secondaryItemAlpha(): Modifier = this.alpha(SecondaryItemAlpha)
