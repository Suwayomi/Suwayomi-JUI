/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import ca.gosyer.ui.base.components.Toolbar
import ca.gosyer.ui.base.prefs.Pref
import ca.gosyer.ui.base.prefs.PreferencesScrollableColumn
import ca.gosyer.ui.main.Route
import com.github.zsoltk.compose.router.BackStack
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Regular
import compose.icons.fontawesomeicons.regular.Edit

@Composable
fun SettingsScreen(navController: BackStack<Route>) {
    Column {
        Toolbar("Settings", closable = false)
        PreferencesScrollableColumn {
            Pref(
                title = "General",
                icon = FontAwesomeIcons.Regular.Edit,
                onClick = { navController.push(Route.SettingsGeneral) }
            )
            Pref(
                title = "Appearance",
                icon = FontAwesomeIcons.Regular.Edit,
                onClick = { navController.push(Route.SettingsAppearance) }
            )
            Pref(
                title = "Server",
                icon = FontAwesomeIcons.Regular.Edit,
                onClick = { navController.push(Route.SettingsServer) }
            )
            Pref(
                title = "Library",
                icon = FontAwesomeIcons.Regular.Edit,
                onClick = { navController.push(Route.SettingsLibrary) }
            )
            Pref(
                title = "Reader",
                icon = FontAwesomeIcons.Regular.Edit,
                onClick = { navController.push(Route.SettingsReader) }
            )
            /*Pref(
                title = "Downloads",
                icon = FontAwesomeIcons.Regular.Edit,
                onClick = { navController.push(Route.SettingsDownloads) }
            )
            Pref(
                title = "Tracking",
                icon = FontAwesomeIcons.Regular.Edit,
                onClick = { navController.push(Route.SettingsTracking) }
            )
            */
            Pref(
                title = "Browse",
                icon = FontAwesomeIcons.Regular.Edit,
                onClick = { navController.push(Route.SettingsBrowse) }
            )
            Pref(
                title = "Backup",
                icon = FontAwesomeIcons.Regular.Edit,
                onClick = { navController.push(Route.SettingsBackup) }
            )
            /*Pref(
                title = "Security",
                icon = FontAwesomeIcons.Regular.Edit,
                onClick = { navController.push(Route.SettingsSecurity) }
            )
            Pref(
                title = "Parental Controls",
                icon = FontAwesomeIcons.Regular.User,
                onClick = { navController.push(Route.SettingsParentalControls) }
            )*/
            Pref(
                title = "Advanced",
                icon = FontAwesomeIcons.Regular.Edit,
                onClick = { navController.push(Route.SettingsAdvanced) }
            )
        }
    }
}
