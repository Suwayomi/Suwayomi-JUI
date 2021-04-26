/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import ca.gosyer.ui.base.components.Toolbar
import ca.gosyer.ui.base.prefs.PreferencesScrollableColumn
import ca.gosyer.ui.main.Route
import com.github.zsoltk.compose.router.BackStack

@Composable
fun SettingsTrackingScreen(navController: BackStack<Route>) {
    Column {
        Toolbar("Tracking Settings", navController, true)
        PreferencesScrollableColumn {
        }
    }
}
