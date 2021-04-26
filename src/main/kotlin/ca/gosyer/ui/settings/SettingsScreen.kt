/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import ca.gosyer.ui.base.components.Toolbar
import ca.gosyer.ui.base.prefs.PreferenceRow
import ca.gosyer.ui.main.Route
import com.github.zsoltk.compose.router.BackStack
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Regular
import compose.icons.fontawesomeicons.regular.Edit

@Composable
fun SettingsScreen(navController: BackStack<Route>) {
    Column {
        Toolbar("Settings", closable = false)
        LazyColumn {
            item {
                PreferenceRow(
                    title = "General",
                    icon = FontAwesomeIcons.Regular.Edit,
                    onClick = { navController.push(Route.SettingsGeneral) }
                )
            }
            item {
                PreferenceRow(
                    title = "Appearance",
                    icon = FontAwesomeIcons.Regular.Edit,
                    onClick = { navController.push(Route.SettingsAppearance) }
                )
            }
            item {
                PreferenceRow(
                    title = "Server",
                    icon = FontAwesomeIcons.Regular.Edit,
                    onClick = { navController.push(Route.SettingsServer) }
                )
            }
            item {
                PreferenceRow(
                    title = "Library",
                    icon = FontAwesomeIcons.Regular.Edit,
                    onClick = { navController.push(Route.SettingsLibrary) }
                )
            }
            item {
                PreferenceRow(
                    title = "Reader",
                    icon = FontAwesomeIcons.Regular.Edit,
                    onClick = { navController.push(Route.SettingsReader) }
                )
            }
            /*item {
                Pref(
                    title = "Downloads",
                    icon = FontAwesomeIcons.Regular.Edit,
                    onClick = { navController.push(Route.SettingsDownloads) }
                )
            }
            item {
                Pref(
                    title = "Tracking",
                    icon = FontAwesomeIcons.Regular.Edit,
                    onClick = { navController.push(Route.SettingsTracking) }
                )
            }*/
            item {
                PreferenceRow(
                    title = "Browse",
                    icon = FontAwesomeIcons.Regular.Edit,
                    onClick = { navController.push(Route.SettingsBrowse) }
                )
            }
            item {
                PreferenceRow(
                    title = "Backup",
                    icon = FontAwesomeIcons.Regular.Edit,
                    onClick = { navController.push(Route.SettingsBackup) }
                )
            }
            /*item {
                Pref(
                    title = "Security",
                    icon = FontAwesomeIcons.Regular.Edit,
                    onClick = { navController.push(Route.SettingsSecurity) }
                )
            }
            item {
                Pref(
                    title = "Parental Controls",
                    icon = FontAwesomeIcons.Regular.User,
                    onClick = { navController.push(Route.SettingsParentalControls) }
                )
            }*/
            item {
                PreferenceRow(
                    title = "Advanced",
                    icon = FontAwesomeIcons.Regular.Edit,
                    onClick = { navController.push(Route.SettingsAdvanced) }
                )
            }
        }
    }
}
