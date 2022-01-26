/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.settings

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Backup
import androidx.compose.material.icons.rounded.ChromeReaderMode
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.CollectionsBookmark
import androidx.compose.material.icons.rounded.Computer
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ca.gosyer.ui.base.components.MenuController
import ca.gosyer.ui.base.components.Toolbar
import ca.gosyer.ui.base.prefs.PreferenceRow
import dev.icerock.moko.resources.compose.stringResource
import ca.gosyer.i18n.MR
import ca.gosyer.ui.main.Routes

@Composable
fun SettingsScreen(menuController: MenuController) {
    Column {
        Toolbar(stringResource(MR.strings.location_settings), closable = false)
        Box {
            val state = rememberLazyListState()
            LazyColumn(Modifier.fillMaxSize(), state) {
                item {
                    PreferenceRow(
                        title = stringResource(MR.strings.settings_general),
                        icon = Icons.Rounded.Tune,
                        onClick = { menuController.push(Routes.SettingsGeneral) }
                    )
                }
                item {
                    PreferenceRow(
                        title = stringResource(MR.strings.settings_appearance),
                        icon = Icons.Rounded.Palette,
                        onClick = { menuController.push(Routes.SettingsAppearance) }
                    )
                }
                item {
                    PreferenceRow(
                        title = stringResource(MR.strings.settings_server),
                        icon = Icons.Rounded.Computer,
                        onClick = { menuController.push(Routes.SettingsServer) }
                    )
                }
                item {
                    PreferenceRow(
                        title = stringResource(MR.strings.settings_library),
                        icon = Icons.Rounded.CollectionsBookmark,
                        onClick = { menuController.push(Routes.SettingsLibrary) }
                    )
                }
                item {
                    PreferenceRow(
                        title = stringResource(MR.strings.settings_reader),
                        icon = Icons.Rounded.ChromeReaderMode,
                        onClick = { menuController.push(Routes.SettingsReader) }
                    )
                }
                /*item {
                    Pref(
                        title = stringResource(MR.strings.settings_download),
                        icon = Icons.Rounded.GetApp,
                        onClick = { navController.push(Route.SettingsDownloads) }
                    )
                }
                item {
                    Pref(
                        title = stringResource(MR.strings.settings_tracking),
                        icon = Icons.Rounded.Sync,
                        onClick = { navController.push(Route.SettingsTracking) }
                    )
                }*/
                item {
                    PreferenceRow(
                        title = stringResource(MR.strings.settings_browse),
                        icon = Icons.Rounded.Explore,
                        onClick = { menuController.push(Routes.SettingsBrowse) }
                    )
                }
                item {
                    PreferenceRow(
                        title = stringResource(MR.strings.settings_backup),
                        icon = Icons.Rounded.Backup,
                        onClick = { menuController.push(Routes.SettingsBackup) }
                    )
                }
                /*item {
                    Pref(
                        title = stringResource(MR.strings.settings_security),
                        icon = Icons.Rounded.Security,
                        onClick = { navController.push(Route.SettingsSecurity) }
                    )
                }
                item {
                    Pref(
                        title = stringResource(MR.strings.settings_parental_controls),
                        icon = Icons.Rounded.PeopleOutline,
                        onClick = { navController.push(Route.SettingsParentalControls) }
                    )
                }*/
                item {
                    PreferenceRow(
                        title = stringResource(MR.strings.settings_advanced),
                        icon = Icons.Rounded.Code,
                        onClick = { menuController.push(Routes.SettingsAdvanced) }
                    )
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
