/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.settings

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Colors
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.primarySurface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ca.gosyer.jui.domain.ui.model.ThemeMode
import ca.gosyer.jui.domain.ui.service.UiPreferences
import ca.gosyer.jui.i18n.MR
import ca.gosyer.jui.ui.base.navigation.Toolbar
import ca.gosyer.jui.ui.base.prefs.ChoicePreference
import ca.gosyer.jui.ui.base.prefs.ColorPreference
import ca.gosyer.jui.ui.base.prefs.SwitchPreference
import ca.gosyer.jui.ui.base.theme.AppColorsPreferenceState
import ca.gosyer.jui.ui.base.theme.asStateFlow
import ca.gosyer.jui.ui.base.theme.getDarkColors
import ca.gosyer.jui.ui.base.theme.getLightColors
import ca.gosyer.jui.ui.main.components.bottomNav
import ca.gosyer.jui.ui.viewModel
import ca.gosyer.jui.uicore.components.HorizontalScrollbar
import ca.gosyer.jui.uicore.components.VerticalScrollbar
import ca.gosyer.jui.uicore.components.mangaAspectRatio
import ca.gosyer.jui.uicore.components.rememberScrollbarAdapter
import ca.gosyer.jui.uicore.components.scrollbarPadding
import ca.gosyer.jui.uicore.components.secondaryItemAlpha
import ca.gosyer.jui.uicore.prefs.PreferenceMutableStateFlow
import ca.gosyer.jui.uicore.resources.stringResource
import ca.gosyer.jui.uicore.theme.ExtraColors
import ca.gosyer.jui.uicore.theme.extraColors
import ca.gosyer.jui.uicore.theme.themes
import ca.gosyer.jui.uicore.vm.ContextWrapper
import ca.gosyer.jui.uicore.vm.ViewModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import cafe.adriel.voyager.core.screen.uniqueScreenKey
import kotlinx.collections.immutable.persistentMapOf
import me.tatarka.inject.annotations.Inject

class SettingsAppearanceScreen : Screen {
    override val key: ScreenKey = uniqueScreenKey

    @Composable
    override fun Content() {
        val vm = viewModel { themesViewModel() }
        val appThemeVM = viewModel { appThemeViewModel() }
        val (colors, extraColors) = appThemeVM.getColors()
        SettingsAppearanceScreenContent(
            activeColors = vm.getActiveColors(),
            themeMode = vm.themeMode,
            lightTheme = vm.lightTheme,
            darkTheme = vm.darkTheme,
            windowDecorations = vm.windowDecorations,
            customColors = colors,
            customExtraColors = extraColors,
        )
    }
}

class ThemesViewModel
    @Inject
    constructor(
        private val uiPreferences: UiPreferences,
        contextWrapper: ContextWrapper,
    ) : ViewModel(contextWrapper) {
        val themeMode = uiPreferences.themeMode().asStateFlow()
        val lightTheme = uiPreferences.lightTheme().asStateFlow()
        val darkTheme = uiPreferences.darkTheme().asStateFlow()
        val lightColors = uiPreferences.getLightColors().asStateFlow(scope)
        val darkColors = uiPreferences.getDarkColors().asStateFlow(scope)

        val windowDecorations = uiPreferences.windowDecorations().asStateFlow()

        @Composable
        fun getActiveColors(): AppColorsPreferenceState = if (MaterialTheme.colors.isLight) lightColors else darkColors
    }

expect val showWindowDecorationsOption: Boolean

@Composable
fun SettingsAppearanceScreenContent(
    activeColors: AppColorsPreferenceState,
    themeMode: PreferenceMutableStateFlow<ThemeMode>,
    lightTheme: PreferenceMutableStateFlow<Int>,
    darkTheme: PreferenceMutableStateFlow<Int>,
    windowDecorations: PreferenceMutableStateFlow<Boolean>,
    customColors: Colors,
    customExtraColors: ExtraColors,
) {
    val isLight = MaterialTheme.colors.isLight
    val themesForCurrentMode = remember(isLight) {
        themes.filter { it.colors.isLight == isLight }
    }
    val currentLightTheme by lightTheme.collectAsState()
    val currentDarkTheme by darkTheme.collectAsState()

    Scaffold(
        modifier = Modifier.windowInsetsPadding(
            WindowInsets.statusBars.add(
                WindowInsets.navigationBars.only(WindowInsetsSides.Horizontal),
            ),
        ),
        topBar = {
            Toolbar(stringResource(MR.strings.settings_appearance_screen))
        },
    ) {
        Box(Modifier.padding(it)) {
            val state = rememberLazyListState()
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = state,
                contentPadding = WindowInsets.bottomNav.add(
                    WindowInsets.navigationBars.only(
                        WindowInsetsSides.Bottom,
                    ),
                ).asPaddingValues(),
            ) {
                item {
                    ChoicePreference(
                        preference = themeMode,
                        choices = persistentMapOf(
                            ThemeMode.System to stringResource(MR.strings.theme_follow_system),
                            ThemeMode.Light to stringResource(MR.strings.theme_light),
                            ThemeMode.Dark to stringResource(MR.strings.theme_dark),
                        ),
                        title = stringResource(MR.strings.theme),
                    )
                }
                item {
                    Text(
                        stringResource(MR.strings.preset_themes),
                        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 4.dp),
                    )
                    val lazyListState = rememberLazyListState()
                    Box {
                        LazyRow(
                            state = lazyListState,
                            modifier = Modifier
                                .animateContentSize()
                                .padding(vertical = 8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            items(themesForCurrentMode) { theme ->
                                Column(
                                    modifier = Modifier
                                        .width(114.dp)
                                        .padding(top = 8.dp),
                                ) {
                                    val isSelected = (isLight && currentLightTheme == theme.id) ||
                                        (!isLight && currentDarkTheme == theme.id)
                                    MaterialTheme(
                                        colors = if (isSelected) customColors else theme.colors,
                                    ) {
                                        ExtraColors.WithExtraColors(
                                            if (isSelected) customExtraColors else theme.extraColors,
                                        ) {
                                            AppThemePreviewItem(
                                                selected = isSelected,
                                                onClick = {
                                                    (if (isLight) lightTheme else darkTheme).value = theme.id
                                                    activeColors.primaryStateFlow.value = theme.colors.primary
                                                    activeColors.secondaryStateFlow.value = theme.colors.secondary
                                                    activeColors.tertiaryStateFlow.value = theme.extraColors.tertiary
                                                },
                                            )
                                        }
                                    }

                                    Text(
                                        text = stringResource(theme.titleRes),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 8.dp)
                                            .secondaryItemAlpha(),
                                        color = MaterialTheme.colors.onSurface,
                                        textAlign = TextAlign.Center,
                                        maxLines = 2,
                                        style = MaterialTheme.typography.body2,
                                    )
                                }
                            }
                        }
                        HorizontalScrollbar(
                            adapter = rememberScrollbarAdapter(lazyListState),
                            modifier = Modifier.align(Alignment.BottomCenter)
                                .fillMaxWidth(),
                        )
                    }
                }
                item {
                    ColorPreference(
                        preference = activeColors.primaryStateFlow,
                        title = stringResource(MR.strings.color_primary),
                        subtitle = stringResource(MR.strings.color_primary_sub),
                        unsetColor = MaterialTheme.colors.primary,
                    )
                }
                item {
                    ColorPreference(
                        preference = activeColors.secondaryStateFlow,
                        title = stringResource(MR.strings.color_secondary),
                        subtitle = stringResource(MR.strings.color_secondary_sub),
                        unsetColor = MaterialTheme.colors.secondary,
                    )
                }
                item {
                    ColorPreference(
                        preference = activeColors.tertiaryStateFlow,
                        title = stringResource(MR.strings.color_tertiary),
                        subtitle = stringResource(MR.strings.color_secondary_sub),
                        unsetColor = MaterialTheme.extraColors.tertiary,
                    )
                }
                if (showWindowDecorationsOption) {
                    item {
                        SwitchPreference(
                            windowDecorations,
                            stringResource(MR.strings.window_decorations),
                            stringResource(MR.strings.window_decorations_sub),
                        )
                    }
                }
            }
            VerticalScrollbar(
                rememberScrollbarAdapter(state),
                Modifier.align(Alignment.CenterEnd)
                    .fillMaxHeight()
                    .scrollbarPadding()
                    .windowInsetsPadding(
                        WindowInsets.bottomNav.add(
                            WindowInsets.navigationBars.only(
                                WindowInsetsSides.Bottom,
                            ),
                        ),
                    ),
            )
        }
    }
}

@Composable
fun AppThemePreviewItem(
    selected: Boolean,
    onClick: () -> Unit,
) {
    val dividerColor = MaterialTheme.colors.onSurface.copy(alpha = 0.2F)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(9f / 16f)
            .border(
                width = 4.dp,
                color = if (selected) {
                    MaterialTheme.colors.primary
                } else {
                    dividerColor
                },
                shape = RoundedCornerShape(17.dp),
            )
            .padding(4.dp)
            .clip(RoundedCornerShape(13.dp))
            .background(MaterialTheme.colors.background)
            .clickable(onClick = onClick),
    ) {
        // App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight(0.8f)
                    .weight(0.7f)
                    .padding(end = 4.dp)
                    .background(
                        color = MaterialTheme.colors.onSurface,
                        shape = MaterialTheme.shapes.small,
                    ),
            )

            Box(
                modifier = Modifier.weight(0.3f),
                contentAlignment = Alignment.CenterEnd,
            ) {
                if (selected) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colors.primary,
                    )
                }
            }
        }

        // Cover
        Box(
            modifier = Modifier
                .padding(start = 8.dp, top = 2.dp)
                .background(
                    color = dividerColor,
                    shape = MaterialTheme.shapes.small,
                )
                .fillMaxWidth(0.5f)
                .aspectRatio(mangaAspectRatio),
        ) {
            Row(
                modifier = Modifier
                    .padding(4.dp)
                    .size(width = 24.dp, height = 16.dp)
                    .clip(RoundedCornerShape(5.dp)),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(12.dp)
                        .background(MaterialTheme.extraColors.tertiary),
                )
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(12.dp)
                        .background(MaterialTheme.colors.secondary),
                )
            }
        }

        // Bottom bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.BottomCenter,
        ) {
            Surface(
                elevation = 3.dp,
            ) {
                Row(
                    modifier = Modifier
                        .height(32.dp)
                        .fillMaxWidth()
                        .background(MaterialTheme.colors.primarySurface)
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(17.dp)
                            .background(
                                color = MaterialTheme.colors.primary,
                                shape = CircleShape,
                            ),
                    )
                    Box(
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .alpha(0.6f)
                            .height(17.dp)
                            .weight(1f)
                            .background(
                                color = MaterialTheme.colors.onSurface,
                                shape = MaterialTheme.shapes.small,
                            ),
                    )
                }
            }
        }
    }
}
