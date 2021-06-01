/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.ChromeReaderMode
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.CollectionsBookmark
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Tune
import androidx.compose.runtime.Composable
import ca.gosyer.ui.base.components.Toolbar
import ca.gosyer.ui.base.prefs.PreferenceRow
import ca.gosyer.ui.main.Route
import com.github.zsoltk.compose.router.BackStack

@Composable
fun SettingsScreen(navController: BackStack<Route>) {
    Column {
        Toolbar("Settings", closable = false)
        LazyColumn {
            item {
                PreferenceRow(
                    title = "General",
                    icon = Icons.Default.Tune,
                    onClick = { navController.push(Route.SettingsGeneral) }
                )
            }
            item {
                PreferenceRow(
                    title = "Appearance",
                    icon = Icons.Default.Palette,
                    onClick = { navController.push(Route.SettingsAppearance) }
                )
            }
            item {
                PreferenceRow(
                    title = "Server",
                    icon = Icons.Default.Computer,
                    onClick = { navController.push(Route.SettingsServer) }
                )
            }
            item {
                PreferenceRow(
                    title = "Library",
                    icon = Icons.Default.CollectionsBookmark,
                    onClick = { navController.push(Route.SettingsLibrary) }
                )
            }
            item {
                PreferenceRow(
                    title = "Reader",
                    icon = Icons.Default.ChromeReaderMode,
                    onClick = { navController.push(Route.SettingsReader) }
                )
            }
            /*item {
                Pref(
                    title = "Downloads",
                    icon = Icons.Default.GetApp,
                    onClick = { navController.push(Route.SettingsDownloads) }
                )
            }
            item {
                Pref(
                    title = "Tracking",
                    icon = Icons.Default.Sync,
                    onClick = { navController.push(Route.SettingsTracking) }
                )
            }*/
            item {
                PreferenceRow(
                    title = "Browse",
                    icon = Icons.Default.Explore,
                    onClick = { navController.push(Route.SettingsBrowse) }
                )
            }
            item {
                PreferenceRow(
                    title = "Backup",
                    icon = Icons.Default.Backup,
                    onClick = { navController.push(Route.SettingsBackup) }
                )
            }
            /*item {
                Pref(
                    title = "Security",
                    icon = Icons.Default.Security,
                    onClick = { navController.push(Route.SettingsSecurity) }
                )
            }
            item {
                Pref(
                    title = "Parental Controls",
                    icon = Icons.Default.PeopleOutline,
                    onClick = { navController.push(Route.SettingsParentalControls) }
                )
            }*/
            item {
                PreferenceRow(
                    title = "Advanced",
                    icon = Icons.Default.Code,
                    onClick = { navController.push(Route.SettingsAdvanced) }
                )
            }
        }
    }
}
