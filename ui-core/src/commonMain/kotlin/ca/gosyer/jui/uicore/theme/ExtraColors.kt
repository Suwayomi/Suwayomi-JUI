/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.uicore.theme

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.structuralEqualityPolicy
import androidx.compose.ui.graphics.Color

@Stable
class ExtraColors(
    tertiary: Color,
    onTertiary: Color,
) {
    var tertiary by mutableStateOf(tertiary, structuralEqualityPolicy())
        internal set
    var onTertiary by mutableStateOf(onTertiary, structuralEqualityPolicy())
        internal set

    fun copy(
        tertiary: Color = this.tertiary,
        onTertiary: Color = this.onTertiary,
    ): ExtraColors = ExtraColors(
        tertiary,
        onTertiary,
    )

    override fun toString(): String {
        return "ExtraColors(" +
            "tertiary=$tertiary, " +
            "onTertiary=$onTertiary, " +
            ")"
    }

    companion object {
        @Composable
        fun WithExtraColors(extraColors: ExtraColors, content: @Composable () -> Unit) {
            CompositionLocalProvider(
                LocalExtraColors provides extraColors,
                content = content,
            )
        }
    }
}

val MaterialTheme.extraColors: ExtraColors
    @Composable
    get() = LocalExtraColors.current

private val LocalExtraColors = staticCompositionLocalOf<ExtraColors> {
    error("The AppColors composable must be called before usage")
}
