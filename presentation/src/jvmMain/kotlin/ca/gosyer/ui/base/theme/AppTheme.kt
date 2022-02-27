/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

@file:JvmName("ThemeScrollbarStyleKt")

package ca.gosyer.ui.base.theme

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
import ca.gosyer.data.ui.UiPreferences
import ca.gosyer.data.ui.model.ThemeMode
import ca.gosyer.ui.base.components.LocalScrollbarStyle
import ca.gosyer.ui.base.theme.ThemeScrollbarStyle.getScrollbarStyle
import ca.gosyer.uicore.theme.Theme
import ca.gosyer.uicore.theme.themes
import ca.gosyer.uicore.vm.ContextWrapper
import ca.gosyer.uicore.vm.LocalViewModelFactory
import ca.gosyer.uicore.vm.ViewModel
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
    val vmFactory = LocalViewModelFactory.current
    val vm = remember { vmFactory.instantiate<AppThemeViewModel>() }
    val colors = vm.getColors()
    /*val systemUiController = rememberSystemUiController()*/

    MaterialTheme(colors = colors) {
        CompositionLocalProvider(
            LocalScrollbarStyle provides getScrollbarStyle(),
            content = content
        )
    }
}

class AppThemeViewModel @Inject constructor(
    private val uiPreferences: UiPreferences,
    contextWrapper: ContextWrapper
) : ViewModel(contextWrapper) {
    override val scope = MainScope()

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

    override fun onDispose() {
        baseThemeScope.cancel()
        scope.cancel()
    }
}
