/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.settings

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ca.gosyer.data.ui.UiPreferences
import ca.gosyer.data.ui.model.ThemeMode
import ca.gosyer.i18n.MR
import ca.gosyer.ui.base.navigation.Toolbar
import ca.gosyer.ui.base.prefs.ChoicePreference
import ca.gosyer.ui.base.prefs.ColorPreference
import ca.gosyer.ui.base.prefs.SwitchPreference
import ca.gosyer.ui.base.theme.AppColorsPreferenceState
import ca.gosyer.ui.base.theme.asStateFlow
import ca.gosyer.ui.base.theme.getDarkColors
import ca.gosyer.ui.base.theme.getLightColors
import ca.gosyer.uicore.components.VerticalScrollbar
import ca.gosyer.uicore.components.rememberScrollbarAdapter
import ca.gosyer.uicore.prefs.PreferenceMutableStateFlow
import ca.gosyer.uicore.resources.stringResource
import ca.gosyer.uicore.theme.Theme
import ca.gosyer.uicore.theme.themes
import ca.gosyer.uicore.vm.ContextWrapper
import ca.gosyer.uicore.vm.ViewModel
import ca.gosyer.uicore.vm.viewModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import cafe.adriel.voyager.core.screen.uniqueScreenKey
import me.tatarka.inject.annotations.Inject

class SettingsAppearanceScreen : Screen {
    override val key: ScreenKey = uniqueScreenKey

    @Composable
    override fun Content() {
        val vm = viewModel<ThemesViewModel>()
        SettingsAppearanceScreenContent(
            activeColors = vm.getActiveColors(),
            themeMode = vm.themeMode,
            lightTheme = vm.lightTheme,
            darkTheme = vm.darkTheme,
            windowDecorations = vm.windowDecorations
        )
    }
}

class ThemesViewModel @Inject constructor(
    private val uiPreferences: UiPreferences,
    contextWrapper: ContextWrapper
) : ViewModel(contextWrapper) {

    val themeMode = uiPreferences.themeMode().asStateFlow()
    val lightTheme = uiPreferences.lightTheme().asStateFlow()
    val darkTheme = uiPreferences.darkTheme().asStateFlow()
    val lightColors = uiPreferences.getLightColors().asStateFlow(scope)
    val darkColors = uiPreferences.getDarkColors().asStateFlow(scope)

    val windowDecorations = uiPreferences.windowDecorations().asStateFlow()

    @Composable
    fun getActiveColors(): AppColorsPreferenceState {
        return if (MaterialTheme.colors.isLight) lightColors else darkColors
    }
}

expect val showWindowDecorationsOption: Boolean

@Composable
fun SettingsAppearanceScreenContent(
    activeColors: AppColorsPreferenceState,
    themeMode: PreferenceMutableStateFlow<ThemeMode>,
    lightTheme: PreferenceMutableStateFlow<Int>,
    darkTheme: PreferenceMutableStateFlow<Int>,
    windowDecorations: PreferenceMutableStateFlow<Boolean>
) {
    val isLight = MaterialTheme.colors.isLight
    val themesForCurrentMode = remember(isLight) {
        themes.filter { it.colors.isLight == isLight }
    }

    Scaffold(
        topBar = {
            Toolbar(stringResource(MR.strings.settings_appearance_screen))
        }
    ) {
        Box(Modifier.padding(it)) {
            val state = rememberLazyListState()
            LazyColumn(Modifier.fillMaxSize(), state) {
                item {
                    ChoicePreference(
                        preference = themeMode,
                        choices = mapOf(
                            ThemeMode.System to stringResource(MR.strings.theme_follow_system),
                            ThemeMode.Light to stringResource(MR.strings.theme_light),
                            ThemeMode.Dark to stringResource(MR.strings.theme_dark)
                        ),
                        title = stringResource(MR.strings.theme)
                    )
                }
                item {
                    Text(
                        stringResource(MR.strings.preset_themes),
                        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 4.dp)
                    )
                    LazyRow(modifier = Modifier.padding(horizontal = 8.dp)) {
                        items(themesForCurrentMode) { theme ->
                            ThemeItem(
                                theme,
                                onClick = {
                                    (if (isLight) lightTheme else darkTheme).value = it.id
                                    activeColors.primaryStateFlow.value = it.colors.primary
                                    activeColors.secondaryStateFlow.value = it.colors.secondary
                                }
                            )
                        }
                    }
                }
                item {
                    ColorPreference(
                        preference = activeColors.primaryStateFlow,
                        title = stringResource(MR.strings.color_primary),
                        subtitle = stringResource(MR.strings.color_primary_sub),
                        unsetColor = MaterialTheme.colors.primary
                    )
                }
                item {
                    ColorPreference(
                        preference = activeColors.secondaryStateFlow,
                        title = stringResource(MR.strings.color_secondary),
                        subtitle = stringResource(MR.strings.color_secondary_sub),
                        unsetColor = MaterialTheme.colors.secondary
                    )
                }
                if (showWindowDecorationsOption) {
                    item {
                        SwitchPreference(
                            windowDecorations,
                            stringResource(MR.strings.window_decorations),
                            stringResource(MR.strings.window_decorations_sub)
                        )
                    }
                }
            }
            VerticalScrollbar(
                rememberScrollbarAdapter(state),
                Modifier.align(Alignment.CenterEnd)
                    .fillMaxHeight()
                    .padding(horizontal = 4.dp, vertical = 8.dp)
            )
        }
    }
}

@Composable
private fun ThemeItem(
    theme: Theme,
    onClick: (Theme) -> Unit
) {
    val borders = MaterialTheme.shapes.small
    val borderColor = if (theme.colors.isLight) {
        Color.Black.copy(alpha = 0.25f)
    } else {
        Color.White.copy(alpha = 0.15f)
    }
    Surface(
        onClick = { onClick(theme) },
        elevation = 4.dp,
        color = theme.colors.background,
        shape = borders,
        modifier = Modifier
            .size(100.dp, 160.dp)
            .padding(8.dp)
            .border(1.dp, borderColor, borders)
    ) {
        Column {
            Box(
                Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(6.dp)
            ) {
                Text(stringResource(MR.strings.theme_text), fontSize = 11.sp)
                Button(
                    onClick = {},
                    enabled = false,
                    contentPadding = PaddingValues(),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .size(40.dp, 20.dp),
                    content = {},
                    colors = ButtonDefaults.buttonColors(
                        disabledBackgroundColor = theme.colors.primary
                    )
                )
                Surface(
                    Modifier
                        .size(24.dp)
                        .align(Alignment.BottomEnd),
                    shape = MaterialTheme.shapes.small.copy(CornerSize(percent = 50)),
                    color = theme.colors.secondary,
                    elevation = 6.dp,
                    content = { }
                )
            }
        }
    }
}
