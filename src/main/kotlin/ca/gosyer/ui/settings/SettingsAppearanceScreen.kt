/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.settings

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
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
import ca.gosyer.ui.base.components.Toolbar
import ca.gosyer.ui.base.prefs.ChoicePreference
import ca.gosyer.ui.base.prefs.ColorPreference
import ca.gosyer.ui.base.theme.AppColorsPreferenceState
import ca.gosyer.ui.base.theme.Theme
import ca.gosyer.ui.base.theme.asStateFlow
import ca.gosyer.ui.base.theme.getDarkColors
import ca.gosyer.ui.base.theme.getLightColors
import ca.gosyer.ui.base.theme.themes
import ca.gosyer.ui.base.vm.ViewModel
import ca.gosyer.ui.base.vm.viewModel
import ca.gosyer.ui.main.Route
import com.github.zsoltk.compose.router.BackStack
import javax.inject.Inject

class ThemesViewModel @Inject constructor(
    private val uiPreferences: UiPreferences,
) : ViewModel() {

    val themeMode = uiPreferences.themeMode().asStateFlow()
    val lightTheme = uiPreferences.lightTheme().asStateFlow()
    val darkTheme = uiPreferences.darkTheme().asStateFlow()
    val lightColors = uiPreferences.getLightColors().asStateFlow(scope)
    val darkColors = uiPreferences.getDarkColors().asStateFlow(scope)

    @Composable
    fun getActiveColors(): AppColorsPreferenceState {
        return if (MaterialTheme.colors.isLight) lightColors else darkColors
    }
}

@Composable
fun SettingsAppearance(navController: BackStack<Route>) {
    val vm = viewModel<ThemesViewModel>()

    val activeColors = vm.getActiveColors()
    val isLight = MaterialTheme.colors.isLight
    val themesForCurrentMode = remember(isLight) {
        themes.filter { it.colors.isLight == isLight }
    }

    Column {
        Toolbar("Appearance Settings", navController, true)
        LazyColumn {
            item {
                ChoicePreference(
                    preference = vm.themeMode,
                    choices = mapOf(
                        //ThemeMode.System to R.string.follow_system_settings,
                        ThemeMode.Light to "Light",
                        ThemeMode.Dark to "Dark"
                    ),
                    title = "Theme"
                )
            }
            item {
                Text(
                    "Preset themes",
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 4.dp)
                )
                LazyRow(modifier = Modifier.padding(horizontal = 8.dp)) {
                    items(themesForCurrentMode) { theme ->
                        ThemeItem(theme, onClick = {
                            (if (isLight) vm.lightTheme else vm.darkTheme).value = it.id
                            activeColors.primaryStateFlow.value = it.colors.primary
                            activeColors.secondaryStateFlow.value = it.colors.secondary
                        })
                    }
                }
            }
            item {
                ColorPreference(
                    preference = activeColors.primaryStateFlow, title = "Color primary",
                    subtitle = "Displayed most frequently across your app",
                    unsetColor = MaterialTheme.colors.primary
                )
            }
            item {
                ColorPreference(
                    preference = activeColors.secondaryStateFlow, title = "Color secondary",
                    subtitle = "Accents select parts of the UI",
                    unsetColor = MaterialTheme.colors.secondary
                )
            }
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
        elevation = 4.dp, color = theme.colors.background, shape = borders,
        modifier = Modifier
            .size(100.dp, 160.dp)
            .padding(8.dp)
            .border(1.dp, borderColor, borders)
            .clickable(onClick = { onClick(theme) })
    ) {
        Column {
            Box(
                Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(6.dp)
            ) {
                Text("Text", fontSize = 11.sp)
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
                Surface(Modifier
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
