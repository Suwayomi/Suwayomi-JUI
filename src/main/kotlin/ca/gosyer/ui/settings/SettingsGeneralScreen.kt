/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import ca.gosyer.data.translation.XmlResourceBundle
import ca.gosyer.data.ui.UiPreferences
import ca.gosyer.data.ui.model.StartScreen
import ca.gosyer.ui.base.components.Toolbar
import ca.gosyer.ui.base.prefs.ChoicePreference
import ca.gosyer.ui.base.prefs.SwitchPreference
import ca.gosyer.ui.base.resources.stringResource
import ca.gosyer.ui.base.vm.ViewModel
import ca.gosyer.ui.base.vm.viewModel
import ca.gosyer.ui.main.Route
import ca.gosyer.util.lang.capitalize
import com.github.zsoltk.compose.router.BackStack
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale
import javax.inject.Inject

class SettingsGeneralViewModel @Inject constructor(
    private val resources: XmlResourceBundle,
    uiPreferences: UiPreferences,
) : ViewModel() {

    val startScreen = uiPreferences.startScreen().asStateFlow()
    val confirmExit = uiPreferences.confirmExit().asStateFlow()
    val language = uiPreferences.language().asStateFlow()
    val dateFormat = uiPreferences.dateFormat().asStateFlow()

    private val now: Instant = Instant.now()
    private val currentLocale = Locale.getDefault()

    fun getLocalePair(locale: String): Pair<String, String> {
        return locale to Locale.forLanguageTag(locale).getDisplayName(currentLocale)
    }

    @Composable
    fun getLanguageChoices(): Map<String, String> {
        val currentLocaleDisplayName = currentLocale.getDisplayName(currentLocale).capitalize(currentLocale)

        return mapOf(
            "" to resources.getString("language_system_default", currentLocaleDisplayName),
            getLocalePair("en-CA")
        )
    }

    @Composable
    fun getDateChoices(): Map<String, String> {
        return mapOf(
            "" to resources.getStringA("date_system_default"),
            "MM/dd/yy" to "MM/dd/yy",
            "dd/MM/yy" to "dd/MM/yy",
            "yyyy-MM-dd" to "yyyy-MM-dd"
        ).mapValues { "${it.value} (${getFormattedDate(it.key)})" }
    }

    private fun getFormattedDate(prefValue: String): String {
        return when (prefValue) {
            "" -> DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)
                .withLocale(Locale.getDefault())
                .withZone(ZoneId.systemDefault())
            else -> DateTimeFormatter.ofPattern(prefValue).withZone(ZoneId.systemDefault())
        }.format(now)
    }
}

@Composable
fun SettingsGeneralScreen(navController: BackStack<Route>) {
    val vm = viewModel<SettingsGeneralViewModel>()
    Column {
        Toolbar(stringResource("settings_general_screen"), navController, true)
        LazyColumn {
            item {
                ChoicePreference(
                    preference = vm.startScreen,
                    title = stringResource("start_screen"),
                    choices = mapOf(
                        StartScreen.Library to stringResource("location_library"),
                        StartScreen.Sources to stringResource("location_sources"),
                        StartScreen.Extensions to stringResource("location_extensions"),
                    )
                )
            }
            item {
                SwitchPreference(preference = vm.confirmExit, title = stringResource("confirm_exit"))
            }
            item {
                Divider()
            }
            item {
                ChoicePreference(
                    preference = vm.language,
                    title = stringResource("language"),
                    choices = vm.getLanguageChoices(),
                )
            }
            item {
                ChoicePreference(
                    preference = vm.dateFormat,
                    title = stringResource("date_format"),
                    choices = vm.getDateChoices()
                )
            }
        }
    }
}
