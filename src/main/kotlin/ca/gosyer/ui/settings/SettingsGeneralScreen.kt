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
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import ca.gosyer.data.ui.UiPreferences
import ca.gosyer.data.ui.model.StartScreen
import ca.gosyer.ui.base.components.Toolbar
import ca.gosyer.ui.base.prefs.PreferencesScrollableColumn
import ca.gosyer.ui.base.prefs.SwitchPref
import ca.gosyer.ui.base.vm.ViewModel
import ca.gosyer.ui.base.vm.viewModel
import ca.gosyer.ui.main.Route
import com.github.zsoltk.compose.router.BackStack
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class SettingsGeneralViewModel @Inject constructor(
    uiPreferences: UiPreferences
) : ViewModel() {

    val startScreen = uiPreferences.startScreen().asStateFlow()
    val confirmExit = uiPreferences.confirmExit().asStateFlow()
    val language = uiPreferences.language().asStateFlow()
    val dateFormat = uiPreferences.dateFormat().asStateFlow()

    private val now = Date()

    @Composable
    fun getLanguageChoices(): Map<String, String> {
        val currentLocaleDisplayName =
            Locale.getDefault().let { it.getDisplayName(it).capitalize() }
        return mapOf(
            "" to "System Default ($currentLocaleDisplayName)"
        )
    }

    @Composable
    fun getDateChoices(): Map<String, String> {
        return mapOf(
            "" to "System Default",
            "MM/dd/yy" to "MM/dd/yy",
            "dd/MM/yy" to "dd/MM/yy",
            "yyyy-MM-dd" to "yyyy-MM-dd"
        ).mapValues { "${it.value} (${getFormattedDate(it.key)})" }
    }

    private fun getFormattedDate(prefValue: String): String {
        return when (prefValue) {
            "" -> DateFormat.getDateInstance(DateFormat.SHORT)
            else -> SimpleDateFormat(prefValue, Locale.getDefault())
        }.format(now.time)
    }
}

@Composable
fun SettingsGeneralScreen(navController: BackStack<Route>) {
    val vm = viewModel<SettingsGeneralViewModel>()
    Column {
        Toolbar("General Settings", navController, true)
        PreferencesScrollableColumn {
            ChoicePref(
                preference = vm.startScreen,
                title = "Start Screen",
                choices = mapOf(
                    StartScreen.Library to "Library",
                    StartScreen.Sources to "Sources",
                    StartScreen.Extensions to "Extensions",
                )
            )
            SwitchPref(preference = vm.confirmExit, title = "Confirm Exit")
            Divider()
            ChoicePref(
                preference = vm.language,
                title = "Language",
                choices = vm.getLanguageChoices(),
            )
            ChoicePref(
                preference = vm.dateFormat,
                title = "Date Format",
                choices = vm.getDateChoices()
            )
        }
    }
}
