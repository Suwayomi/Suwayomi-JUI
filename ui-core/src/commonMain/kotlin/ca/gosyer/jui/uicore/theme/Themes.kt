/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.uicore.theme

import androidx.compose.material.Colors
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.ui.graphics.Color
import ca.gosyer.jui.i18n.MR
import dev.icerock.moko.resources.StringResource

data class Theme(
    val id: Int,
    val titleRes: StringResource,
    val colors: Colors,
    val extraColors: ExtraColors
)

fun tachiyomiLightColors(
    primary: Color = Color(0xFF0057CE),
    primaryVariant: Color = Color(0xFF001947),
    secondary: Color = Color(0xFF0057CE),
    secondaryVariant: Color = Color(0xFF018786),
    background: Color = Color(0xFFFDFBFF),
    surface: Color = Color(0xFFFDFBFF),
    error: Color = Color(0xFFB00020),
    onPrimary: Color = Color.White,
    onSecondary: Color = Color.White,
    onBackground: Color = Color(0xFF1B1B1E),
    onSurface: Color = Color(0xFF1B1B1E),
    onError: Color = Color.White
) = lightColors(
    primary = primary,
    primaryVariant = primaryVariant,
    secondary = secondary,
    secondaryVariant = secondaryVariant,
    background = background,
    surface = surface,
    error = error,
    onPrimary = onPrimary,
    onSecondary = onSecondary,
    onBackground = onBackground,
    onSurface = onSurface,
    onError = onError
)

fun tachiyomiDarkColors(
    primary: Color = Color(0xFFAEC6FF),
    primaryVariant: Color = Color(0xFF00419E),
    secondary: Color = Color(0xFFAEC6FF),
    secondaryVariant: Color = Color(0xFF00419E),
    background: Color = Color(0xFF1B1B1E),
    surface: Color = Color(0xFF1B1B1E),
    error: Color = Color(0xFFCF6679),
    onPrimary: Color = Color(0xFF002C71),
    onSecondary: Color = Color(0xFF002C71),
    onBackground: Color = Color(0xFFE4E2E6),
    onSurface: Color = Color(0xFFE4E2E6),
    onError: Color = Color.White
) = darkColors(
    primary = primary,
    primaryVariant = primaryVariant,
    secondary = secondary,
    secondaryVariant = secondaryVariant,
    background = background,
    surface = surface,
    error = error,
    onPrimary = onPrimary,
    onSecondary = onSecondary,
    onBackground = onBackground,
    onSurface = onSurface,
    onError = onError
)

fun extraColors(
    tertiary: Color = Color(0xFF006E17),
    onTertiary: Color = Color.White
) = ExtraColors(
    tertiary = tertiary,
    onTertiary = onTertiary
)

val themes = listOf(
    // Tachiyomi 0.x default colors
    Theme(
        1,
        MR.strings.theme_default,
        tachiyomiLightColors(),
        extraColors()
    ),
    // Tachiyomi 0.x legacy blue theme
    Theme(
        2,
        MR.strings.theme_legacy_blue,
        lightColors(
            primary = Color(0xFF2979FF),
            primaryVariant = Color(0xFF2979FF),
            onPrimary = Color.White,
            secondary = Color(0xFF2979FF),
            secondaryVariant = Color(0xFF2979FF),
            onSecondary = Color.White
        ),
        extraColors()
    ),
    // Tachiyomi 0.x dark theme
    Theme(
        3,
        MR.strings.theme_default,
        tachiyomiDarkColors(),
        extraColors(
            tertiary = Color(0xFF7ADC77),
            onTertiary = Color(0xFF003907)
        )
    ),
    // AMOLED theme
    Theme(
        4,
        MR.strings.theme_amoled,
        tachiyomiDarkColors(
            primary = Color.Black,
            onPrimary = Color.White,
            background = Color.Black,
            surface = Color.Black
        ),
        extraColors()
    )
)
