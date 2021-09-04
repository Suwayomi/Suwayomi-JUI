/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
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
import ca.gosyer.ui.base.components.Toolbar
import ca.gosyer.ui.base.prefs.PreferenceRow
import ca.gosyer.ui.base.resources.stringResource
import ca.gosyer.ui.main.Routes
import com.github.zsoltk.compose.router.BackStack

@Composable
fun SettingsScreen(navController: BackStack<Routes>) {
    Column {
        Toolbar(stringResource("location_settings"), closable = false)
        LazyColumn {
            item {
                PreferenceRow(
                    title = stringResource("settings_general"),
                    icon = Icons.Rounded.Tune,
                    onClick = { navController.push(Routes.SettingsGeneral) }
                )
            }
            item {
                PreferenceRow(
                    title = stringResource("settings_appearance"),
                    icon = Icons.Rounded.Palette,
                    onClick = { navController.push(Routes.SettingsAppearance) }
                )
            }
            item {
                PreferenceRow(
                    title = stringResource("settings_server"),
                    icon = Icons.Rounded.Computer,
                    onClick = { navController.push(Routes.SettingsServer) }
                )
            }
            item {
                PreferenceRow(
                    title = stringResource("settings_library"),
                    icon = Icons.Rounded.CollectionsBookmark,
                    onClick = { navController.push(Routes.SettingsLibrary) }
                )
            }
            item {
                PreferenceRow(
                    title = stringResource("settings_reader"),
                    icon = Icons.Rounded.ChromeReaderMode,
                    onClick = { navController.push(Routes.SettingsReader) }
                )
            }
            /*item {
                Pref(
                    title = stringResource("settings_download"),
                    icon = Icons.Rounded.GetApp,
                    onClick = { navController.push(Route.SettingsDownloads) }
                )
            }
            item {
                Pref(
                    title = stringResource("settings_tracking"),
                    icon = Icons.Rounded.Sync,
                    onClick = { navController.push(Route.SettingsTracking) }
                )
            }*/
            item {
                PreferenceRow(
                    title = stringResource("settings_browse"),
                    icon = Icons.Rounded.Explore,
                    onClick = { navController.push(Routes.SettingsBrowse) }
                )
            }
            item {
                PreferenceRow(
                    title = stringResource("settings_backup"),
                    icon = Icons.Rounded.Backup,
                    onClick = { navController.push(Routes.SettingsBackup) }
                )
            }
            /*item {
                Pref(
                    title = stringResource("settings_security"),
                    icon = Icons.Rounded.Security,
                    onClick = { navController.push(Route.SettingsSecurity) }
                )
            }
            item {
                Pref(
                    title = stringResource("settings_parental_controls"),
                    icon = Icons.Rounded.PeopleOutline,
                    onClick = { navController.push(Route.SettingsParentalControls) }
                )
            }*/
            item {
                PreferenceRow(
                    title = stringResource("settings_advanced"),
                    icon = Icons.Rounded.Code,
                    onClick = { navController.push(Routes.SettingsAdvanced) }
                )
            }
        }
    }
}
