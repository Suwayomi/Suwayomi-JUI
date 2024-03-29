/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.base.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.takeOrElse
import ca.gosyer.jui.domain.ui.model.ThemeMode
import ca.gosyer.jui.domain.ui.service.UiPreferences
import ca.gosyer.jui.ui.base.LocalViewModels
import ca.gosyer.jui.ui.base.theme.ThemeScrollbarStyle.getScrollbarStyle
import ca.gosyer.jui.uicore.components.LocalScrollbarStyle
import ca.gosyer.jui.uicore.theme.ExtraColors
import ca.gosyer.jui.uicore.theme.Theme
import ca.gosyer.jui.uicore.theme.themes
import ca.gosyer.jui.uicore.vm.ContextWrapper
import ca.gosyer.jui.uicore.vm.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelChildren
import me.tatarka.inject.annotations.Inject

/**
 * Composable used to apply the application colors to [content].
 * It applies the [DesktopMaterialTheme] colors.
 */
@Composable
fun AppTheme(content: @Composable () -> Unit) {
    val viewModels = LocalViewModels.current
    val vm = remember { viewModels.appThemeViewModel() }
    val (colors, extraColors) = vm.getColors()
    // val systemUiController = rememberSystemUiController()
    DisposableEffect(vm) {
        onDispose(vm::onDispose)
    }

    MaterialTheme(colors = colors) {
        ExtraColors.WithExtraColors(extraColors) {
            CompositionLocalProvider(
                LocalScrollbarStyle provides getScrollbarStyle(),
                content = content,
            )
        }
    }
}

class AppThemeViewModel
    @Inject
    constructor(
        private val uiPreferences: UiPreferences,
        contextWrapper: ContextWrapper,
    ) : ViewModel(contextWrapper) {
        override val scope = MainScope()

        private val themeMode = uiPreferences.themeMode().asStateFlow()
        private val lightTheme = uiPreferences.lightTheme().asStateFlow()
        private val darkTheme = uiPreferences.darkTheme().asStateFlow()

        private val baseThemeJob = SupervisorJob()
        private val baseThemeScope = CoroutineScope(baseThemeJob)

        @Composable
        fun getColors(): Pair<Colors, ExtraColors> {
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
            val tertiary by colors.tertiaryStateFlow.collectAsState()

            return getMaterialColors(baseTheme.colors, primary, secondary) to getExtraColors(baseTheme.extraColors, tertiary)
        }

        @Composable
        private fun getBaseTheme(
            themeMode: ThemeMode,
            lightTheme: Int,
            darkTheme: Int,
        ): Theme {
            fun getTheme(
                id: Int,
                isLight: Boolean,
            ): Theme =
                themes.find { it.id == id && it.colors.isLight == isLight }
                    ?: themes.first { it.colors.isLight == isLight }

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
            colorSecondary: Color,
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

        private fun getExtraColors(
            baseExtraColors: ExtraColors,
            colorTertiary: Color,
        ): ExtraColors {
            val tertiary = colorTertiary.takeOrElse { baseExtraColors.tertiary }
            return baseExtraColors.copy(
                tertiary = tertiary,
            )
        }

        override fun onDispose() {
            baseThemeScope.cancel()
            scope.cancel()
        }
    }
