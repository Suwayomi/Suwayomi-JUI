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

/* Theme Template

    Theme(
        TODO(),
        MR.strings.,
        tachiyomiDarkColors(
            primary = Color(0xFF),
            primaryVariant = Color(0xFF),
            secondary = Color(0xFF),
            secondaryVariant = Color(0xFF),
            background = Color(0xFF),
            surface = Color(0xFF),
            onPrimary = Color(0xFF),
            onSecondary = Color(0xFF),
            onBackground = Color(0xFF),
            onSurface = Color(0xFF),
        ),
        extraColors(
            tertiary = Color(0xFF),
            onTertiary = Color(0xFF)
        )
    )

 */

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
    ),
    /**
     * Green Apple theme
     * Original color scheme by CarlosEsco, Jays2Kings and CrepeTF
     */
    Theme(
        5,
        MR.strings.theme_green_apple,
        tachiyomiLightColors(
            primary = Color(0xFF006D2F),
            primaryVariant = Color(0xFF96F8A9),
            secondary = Color(0xFF006D2F),
            secondaryVariant = Color(0xFF96F8A9),
            background = Color(0xFFFBFDF7),
            surface = Color(0xFFFBFDF7),
            onBackground = Color(0xFF1A1C19),
            onSurface = Color(0xFF1A1C19),
        ),
        extraColors(
            tertiary = Color(0xFFB91D22)
        )
    ),
    Theme(
        6,
        MR.strings.theme_green_apple,
        tachiyomiDarkColors(
            primary = Color(0xFF7ADB8F),
            primaryVariant = Color(0xFF005322),
            secondary = Color(0xFF7ADB8F),
            secondaryVariant = Color(0xFF005322),
            background = Color(0xFF1A1C19),
            surface = Color(0xFF1A1C19),
            onPrimary = Color(0xFF003915),
            onSecondary = Color(0xFF003915),
            onBackground = Color(0xFFE1E3DD),
            onSurface = Color(0xFFE1E3DD),
        ),
        extraColors(
            tertiary = Color(0xFFFFB3AA),
            onTertiary = Color(0xFF680006)
        )
    ),
    /**
     * Lavender theme
     * Original color scheme by CrepeTF
     */
    Theme(
        7,
        MR.strings.theme_lavender,
        tachiyomiLightColors(
            primary = Color(0xFF7B46AF),
            primaryVariant = Color(0xFF7B46AF),
            secondary = Color(0xFF7B46AF),
            secondaryVariant = Color(0xFF7B46AF),
            background = Color(0xFFEDE2FF),
            surface = Color(0xFFEDE2FF),
            onPrimary = Color(0xFFEDE2FF),
            onSecondary = Color(0xFFEDE2FF),
            onBackground = Color(0xFF1B1B22),
            onSurface = Color(0xFF1B1B22),
        ),
        extraColors(
            tertiary = Color(0xFFEDE2FF),
            onTertiary = Color(0xFF7B46AF)
        )
    ),
    Theme(
        8,
        MR.strings.theme_lavender,
        tachiyomiDarkColors(
            primary = Color(0xFFA177FF),
            primaryVariant = Color(0xFFA177FF),
            secondary = Color(0xFFA177FF),
            secondaryVariant = Color(0xFFA177FF),
            background = Color(0xFF111129),
            surface = Color(0xFF111129),
            onPrimary = Color(0xFF111129),
            onSecondary = Color(0xFF111129),
            onBackground = Color(0xFFDEE8FF),
            onSurface = Color(0xFFDEE8FF),
        ),
        extraColors(
            tertiary = Color(0xFF5E25E1),
            onTertiary = Color(0xFFE8E8E8)
        )
    ),
    /**
     * Midnight Dusk theme
     * Original color scheme by CrepeTF
     */
    Theme(
        9,
        MR.strings.theme_midnight_dusk,
        tachiyomiLightColors(
            primary = Color(0xFFBB0054),
            primaryVariant = Color(0xFFFFD9E1),
            secondary = Color(0xFFBB0054),
            secondaryVariant = Color(0xFFFFD9E1),
            background = Color(0xFFFFFBFF),
            surface = Color(0xFFFFFBFF),
            onBackground = Color(0xFF1C1B1F),
            onSurface = Color(0xFF1C1B1F),
        ),
        extraColors(
            tertiary = Color(0xFF006638),
        )
    ),
    Theme(
        10,
        MR.strings.theme_midnight_dusk,
        tachiyomiDarkColors(
            primary = Color(0xFFF02475),
            primaryVariant = Color(0xFFBD1C5C),
            secondary = Color(0xFFF02475),
            secondaryVariant = Color(0xFFF02475),
            background = Color(0xFF16151D),
            surface = Color(0xFF16151D),
            onPrimary = Color(0xFFFFFFFF),
            onSecondary = Color(0xFFFFFFFF),
            onBackground = Color(0xFFE5E1E5),
            onSurface = Color(0xFFE5E1E5),
        ),
        extraColors(
            tertiary = Color(0xFF55971C),
        )
    ),
    /**
     * Strawberry Daiquiri theme
     * Original color scheme by Soitora
     */
    Theme(
        11,
        MR.strings.theme_strawberry_daiquiri,
        tachiyomiLightColors(
            primary = Color(0xFFB61E40),
            primaryVariant = Color(0xFFFFDADD),
            secondary = Color(0xFFB61E40),
            secondaryVariant = Color(0xFFFFDADD),
            background = Color(0xFFFCFCFC),
            surface = Color(0xFFFCFCFC),
            onBackground = Color(0xFF201A1A),
            onSurface = Color(0xFF201A1A),
        ),
        extraColors(
            tertiary = Color(0xFF775930),
        )
    ),
    Theme(
        12,
        MR.strings.theme_strawberry_daiquiri,
        tachiyomiDarkColors(
            primary = Color(0xFFFFB2B9),
            primaryVariant = Color(0xFF91002A),
            secondary = Color(0xFFFFB2B9),
            secondaryVariant = Color(0xFF91002A),
            background = Color(0xFF201A1A),
            surface = Color(0xFF201A1A),
            onPrimary = Color(0xFF67001B),
            onSecondary = Color(0xFF67001B),
            onBackground = Color(0xFFECDFDF),
            onSurface = Color(0xFFECDFDF),
        ),
        extraColors(
            tertiary = Color(0xFFE8C08E),
            onTertiary = Color(0xFF432C06)
        )
    ),
    /**
     * Strawberry Tako theme
     * Original color scheme by ghostbear
     */
    Theme(
        13,
        MR.strings.theme_tako,
        tachiyomiLightColors(
            primary = Color(0xFF66577E),
            primaryVariant = Color(0xFF66577E),
            secondary = Color(0xFF66577E),
            secondaryVariant = Color(0xFF66577E),
            background = Color(0xFFF7F5FF),
            surface = Color(0xFFF7F5FF),
            onPrimary = Color(0xFFF3B375),
            onSecondary = Color(0xFFF3B375),
            onBackground = Color(0xFF1B1B22),
            onSurface = Color(0xFF1B1B22),
        ),
        extraColors(
            tertiary = Color(0xFFF3B375),
            onTertiary = Color(0xFF574360)
        )
    ),
    Theme(
        14,
        MR.strings.theme_tako,
        tachiyomiDarkColors(
            primary = Color(0xFFF3B375),
            primaryVariant = Color(0xFFF3B375),
            secondary = Color(0xFFF3B375),
            secondaryVariant = Color(0xFFF3B375),
            background = Color(0xFF21212E),
            surface = Color(0xFF21212E),
            onPrimary = Color(0xFF38294E),
            onSecondary = Color(0xFF38294E),
            onBackground = Color(0xFFE3E0F2),
            onSurface = Color(0xFFE3E0F2),
        ),
        extraColors(
            tertiary = Color(0xFF66577E),
            onTertiary = Color(0xFFF3B375)
        )
    ),
    /**
     * Teal & Turquoise theme
     */
    Theme(
        15,
        MR.strings.theme_tealturquoise,
        tachiyomiLightColors(
            primary = Color(0xFF008080),
            primaryVariant = Color(0xFF008080),
            secondary = Color(0xFF008080),
            secondaryVariant = Color(0xFFBFDFDF),
            background = Color(0xFFFAFAFA),
            surface = Color(0xFFFAFAFA),
            onBackground = Color(0xFF050505),
            onSurface = Color(0xFF050505),
        ),
        extraColors(
            tertiary = Color(0xFFFF7F7F),
            onTertiary = Color.Black
        )
    ),
    Theme(
        16,
        MR.strings.theme_tealturquoise,
        tachiyomiDarkColors(
            primary = Color(0xFF40E0D0),
            primaryVariant = Color(0xFF40E0D0),
            secondary = Color(0xFF40E0D0),
            secondaryVariant = Color(0xFF18544E),
            background = Color(0xFF202125),
            surface = Color(0xFF202125),
            onPrimary = Color.Black,
            onSecondary = Color.Black,
            onBackground = Color(0xFFDFDEDA),
            onSurface = Color(0xFFDFDEDA),
        ),
        extraColors(
            tertiary = Color(0xFFBF1F2F),
        )
    ),
    /**
     * Tidal Wave theme
     * Original color scheme by NahutabDevelop
     */
    Theme(
        17,
        MR.strings.theme_tidal_wave,
        tachiyomiLightColors(
            primary = Color(0xFF006780),
            primaryVariant = Color(0xFFB4D4DF),
            secondary = Color(0xFF006780),
            secondaryVariant = Color(0xFFB8EAFF),
            background = Color(0xFFFDFBFF),
            surface = Color(0xFFFDFBFF),
            onBackground = Color(0xFF001C3b),
            onSurface = Color(0xFF001C3b),
        ),
        extraColors(
            tertiary = Color(0xFF92F7BC),
            onTertiary = Color(0xFF001C3B)
        )
    ),
    Theme(
        18,
        MR.strings.theme_tidal_wave,
        tachiyomiDarkColors(
            primary = Color(0xFF5ED4FC),
            primaryVariant = Color(0xFF004D61),
            secondary = Color(0xFF5ED4FC),
            secondaryVariant = Color(0xFF004D61),
            background = Color(0xFF001C3B),
            surface = Color(0xFF001C3B),
            onPrimary = Color(0xFF003544),
            onSecondary = Color(0xFF003544),
            onBackground = Color(0xFFD5E3FF),
            onSurface = Color(0xFFD5E3FF),
        ),
        extraColors(
            tertiary = Color(0xFF004D61),
            onTertiary = Color(0xFF001C3B)
        )
    ),
    /**
     * Yin & Yang theme
     * Original color scheme by Riztard
     */
    Theme(
        19,
        MR.strings.theme_yinyang,
        tachiyomiLightColors(
            primary = Color.Black,
            primaryVariant = Color.Black,
            secondary = Color.Black,
            secondaryVariant = Color.Black,
            background = Color(0xFFFDFDFD),
            surface = Color(0xFFFDFDFD),
            onPrimary = Color.White,
            onSecondary = Color.White,
            onBackground = Color(0xFF222222),
            onSurface = Color(0xFF222222),
        ),
        extraColors(
            tertiary = Color.White,
            onTertiary = Color.Black
        )
    ),
    Theme(
        20,
        MR.strings.theme_yinyang,
        tachiyomiDarkColors(
            primary = Color.White,
            primaryVariant = Color.White,
            secondary = Color.White,
            secondaryVariant = Color(0xFF717171),
            background = Color(0xFF1E1E1E),
            surface = Color(0xFF1E1E1E),
            onPrimary = Color(0xFF5A5A5A),
            onSecondary = Color(0xFF5A5A5A),
            onBackground = Color(0xFFE6E6E6),
            onSurface = Color(0xFFE6E6E6),
        ),
        extraColors(
            tertiary = Color.Black,
            onTertiary = Color.White
        )
    ),
    /**
     * Yotsuba theme
     * Original color scheme by ztimms73
     */
    Theme(
        21,
        MR.strings.theme_yotsuba,
        tachiyomiLightColors(
            primary = Color(0xFFAE3200),
            primaryVariant = Color(0xFFFFDBCF),
            secondary = Color(0xFFAE3200),
            secondaryVariant = Color(0xFFFFDBCF),
            background = Color(0xFFFCFCFC),
            surface = Color(0xFFFCFCFC),
            onBackground = Color(0xFF211A18),
            onSurface = Color(0xFF211A18),
        ),
        extraColors(
            tertiary = Color(0xFF6B5E2F),
        )
    ),
    Theme(
        22,
        MR.strings.theme_yotsuba,
        tachiyomiDarkColors(
            primary = Color(0xFFFFB59D),
            primaryVariant = Color(0xFF862200),
            secondary = Color(0xFFFFB59D),
            secondaryVariant = Color(0xFF862200),
            background = Color(0xFF211A18),
            surface = Color(0xFF211A18),
            onPrimary = Color(0xFF5F1600),
            onSecondary = Color(0xFF5F1600),
            onBackground = Color(0xFFEDE0DD),
            onSurface = Color(0xFFEDE0DD),
        ),
        extraColors(
            tertiary = Color(0xFFD7C68D),
            onTertiary = Color(0xFF3A2F05)
        )
    )
)
