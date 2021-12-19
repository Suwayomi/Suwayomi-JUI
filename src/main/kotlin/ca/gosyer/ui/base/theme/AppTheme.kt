/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.base.theme

import androidx.compose.desktop.DesktopMaterialTheme
import androidx.compose.foundation.LocalScrollbarStyle
import androidx.compose.foundation.ScrollbarStyle
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.unit.dp
import ca.gosyer.data.ui.UiPreferences
import ca.gosyer.data.ui.model.ThemeMode
import ca.gosyer.ui.base.vm.ViewModel
import ca.gosyer.ui.base.vm.viewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelChildren
import javax.inject.Inject

/**
 * Composable used to apply the application colors to [content].
 * It applies the [DesktopMaterialTheme] colors.
 */
@Composable
fun AppTheme(content: @Composable () -> Unit) {
    val vm = viewModel<AppThemeViewModel>()
    val colors = vm.getColors()
    /*val systemUiController = rememberSystemUiController()*/

    MaterialTheme(colors = colors) {
        CompositionLocalProvider(
            LocalScrollbarStyle provides ScrollbarStyle(
                minimalHeight = 16.dp,
                thickness = 8.dp,
                shape = MaterialTheme.shapes.small,
                hoverDurationMillis = 300,
                unhoverColor = MaterialTheme.colors.onSurface.copy(alpha = 0.30f),
                hoverColor = MaterialTheme.colors.onSurface.copy(alpha = 0.70f)
            ),
            content = content
        )
    }
}

private class AppThemeViewModel @Inject constructor(
    private val uiPreferences: UiPreferences
) : ViewModel() {
    private val themeMode = uiPreferences.themeMode().asStateFlow()
    private val lightTheme = uiPreferences.lightTheme().asStateFlow()
    private val darkTheme = uiPreferences.darkTheme().asStateFlow()

    private val baseThemeJob = SupervisorJob()
    private val baseThemeScope = CoroutineScope(baseThemeJob)

    @Composable
    fun getColors(): Colors {
        val themeMode by themeMode.collectAsState()
        val lightTheme by lightTheme.collectAsState()
        val darkTheme by darkTheme.collectAsState()

        val baseTheme = getBaseTheme(themeMode, lightTheme, darkTheme)
        val colors = remember(baseTheme.colors.isLight) {
            baseThemeJob.cancelChildren()

            if (baseTheme.colors.isLight) {
                uiPreferences.getLightColors().asStateFlow(baseThemeScope)
            } else {
                uiPreferences.getDarkColors().asStateFlow(baseThemeScope)
            }
        }

        val primary by colors.primaryStateFlow.collectAsState()
        val secondary by colors.secondaryStateFlow.collectAsState()

        return getMaterialColors(baseTheme.colors, primary, secondary)
    }

    @Composable
    private fun getBaseTheme(
        themeMode: ThemeMode,
        lightTheme: Int,
        darkTheme: Int
    ): Theme {
        fun getTheme(id: Int, fallbackIsLight: Boolean): Theme {
            return themes.find { it.id == id }
                ?: themes.first { it.colors.isLight == fallbackIsLight }
        }

        return when (themeMode) {
            ThemeMode.System -> if (!isSystemInDarkTheme()) {
                getTheme(lightTheme, true)
            } else {
                getTheme(darkTheme, false)
            }
            ThemeMode.Light -> getTheme(lightTheme, true)
            ThemeMode.Dark -> getTheme(darkTheme, false)
        }
    }

    private fun getMaterialColors(
        baseColors: Colors,
        colorPrimary: Color,
        colorSecondary: Color
    ): Colors {
        val primary = colorPrimary.takeOrElse { baseColors.primary }
        val secondary = colorSecondary.takeOrElse { baseColors.secondary }
        return baseColors.copy(
            primary = primary,
            primaryVariant = primary,
            secondary = secondary,
            secondaryVariant = secondary,
            onPrimary = if (primary.luminance() > 0.5) Color.Black else Color.White,
            onSecondary = if (secondary.luminance() > 0.5) Color.Black else Color.White,
        )
    }

    override fun onDestroy() {
        baseThemeScope.cancel()
    }
}
