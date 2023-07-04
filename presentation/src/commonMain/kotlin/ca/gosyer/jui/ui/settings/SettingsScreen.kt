/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Backup
import androidx.compose.material.icons.rounded.ChromeReaderMode
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.CollectionsBookmark
import androidx.compose.material.icons.rounded.Computer
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import ca.gosyer.jui.i18n.MR
import ca.gosyer.jui.ui.base.navigation.Toolbar
import ca.gosyer.jui.ui.base.prefs.PreferenceRow
import ca.gosyer.jui.ui.main.components.bottomNav
import ca.gosyer.jui.uicore.components.VerticalScrollbar
import ca.gosyer.jui.uicore.components.rememberScrollbarAdapter
import ca.gosyer.jui.uicore.components.scrollbarPadding
import ca.gosyer.jui.uicore.insets.navigationBars
import ca.gosyer.jui.uicore.insets.statusBars
import ca.gosyer.jui.uicore.resources.stringResource
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import cafe.adriel.voyager.core.screen.uniqueScreenKey
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow

class SettingsScreen : Screen {
    override val key: ScreenKey = uniqueScreenKey

    @Composable
    override fun Content() {
        SettingsScreenContent(navigator = LocalNavigator.currentOrThrow)
    }
}

@Composable
fun SettingsScreenContent(navigator: Navigator) {
    Scaffold(
        modifier = Modifier.windowInsetsPadding(
            WindowInsets.statusBars.add(
                WindowInsets.navigationBars.only(WindowInsetsSides.Horizontal),
            ),
        ),
        topBar = {
            Toolbar(stringResource(MR.strings.location_settings))
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
                    PreferenceRow(
                        title = stringResource(MR.strings.settings_general),
                        icon = Icons.Rounded.Tune,
                        onClick = { navigator push SettingsGeneralScreen() },
                    )
                }
                item {
                    PreferenceRow(
                        title = stringResource(MR.strings.settings_appearance),
                        icon = Icons.Rounded.Palette,
                        onClick = { navigator push SettingsAppearanceScreen() },
                    )
                }
                item {
                    PreferenceRow(
                        title = stringResource(MR.strings.settings_server),
                        icon = Icons.Rounded.Computer,
                        onClick = { navigator push SettingsServerScreen() },
                    )
                }
                item {
                    PreferenceRow(
                        title = stringResource(MR.strings.settings_library),
                        icon = Icons.Rounded.CollectionsBookmark,
                        onClick = { navigator push SettingsLibraryScreen() },
                    )
                }
                item {
                    PreferenceRow(
                        title = stringResource(MR.strings.settings_reader),
                        icon = Icons.Rounded.ChromeReaderMode,
                        onClick = { navigator push SettingsReaderScreen() },
                    )
                }
                /*item {
                    Pref(
                        title = stringResource(MR.strings.settings_download),
                        icon = Icons.Rounded.GetApp,
                        onClick = { navigator push SettingsDownloadsScreen() }
                    )
                }
                item {
                    Pref(
                        title = stringResource(MR.strings.settings_tracking),
                        icon = Icons.Rounded.Sync,
                        onClick = { navigator push SettingsTrackingScreen() }
                    )
                }
                item {
                    PreferenceRow(
                        title = stringResource(MR.strings.settings_browse),
                        icon = Icons.Rounded.Explore,
                        onClick = { navigator push SettingsBrowseScreen() }
                    )
                }*/
                item {
                    PreferenceRow(
                        title = stringResource(MR.strings.settings_backup),
                        icon = Icons.Rounded.Backup,
                        onClick = { navigator push SettingsBackupScreen() },
                    )
                }
                /*item {
                    Pref(
                        title = stringResource(MR.strings.settings_security),
                        icon = Icons.Rounded.Security,
                        onClick = { navigator push SettingsSecurityScreen() }
                    )
                }
                item {
                    Pref(
                        title = stringResource(MR.strings.settings_parental_controls),
                        icon = Icons.Rounded.PeopleOutline,
                        onClick = { navigator push SettingsParentalControlsScreen() }
                    )
                }*/
                item {
                    PreferenceRow(
                        title = stringResource(MR.strings.settings_advanced),
                        icon = Icons.Rounded.Code,
                        onClick = { navigator push SettingsAdvancedScreen() },
                    )
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
