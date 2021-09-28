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
import ca.gosyer.build.BuildResources
import ca.gosyer.data.ui.UiPreferences
import ca.gosyer.data.ui.model.StartScreen
import ca.gosyer.ui.base.components.MenuController
import ca.gosyer.ui.base.components.Toolbar
import ca.gosyer.ui.base.prefs.ChoicePreference
import ca.gosyer.ui.base.prefs.SwitchPreference
import ca.gosyer.ui.base.resources.stringResource
import ca.gosyer.ui.base.vm.ViewModel
import ca.gosyer.ui.base.vm.viewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale
import javax.inject.Inject

class SettingsGeneralViewModel @Inject constructor(
    uiPreferences: UiPreferences,
) : ViewModel() {

    val startScreen = uiPreferences.startScreen().asStateFlow()
    val confirmExit = uiPreferences.confirmExit().asStateFlow()
    val language = uiPreferences.language().asStateFlow()
    val dateFormat = uiPreferences.dateFormat().asStateFlow()

    private val now: Instant = Instant.now()
    private val currentLocale = Locale.getDefault()

    @Composable
    fun getStartScreenChoices() = mapOf(
        StartScreen.Library to stringResource("location_library"),
        StartScreen.Updates to stringResource("location_updates"),
        StartScreen.Sources to stringResource("location_sources"),
        StartScreen.Extensions to stringResource("location_extensions")
    )

    @Composable
    fun getLanguageChoices(): Map<String, String> = (
        mapOf(
            "" to stringResource("language_system_default", currentLocale.getDisplayName(currentLocale))
        ) + BuildResources.LANGUAGES
            .associateWith { Locale.forLanguageTag(it).getDisplayName(currentLocale) }
        )
        .toSortedMap(compareBy { it.lowercase() })

    @Composable
    fun getDateChoices(): Map<String, String> {
        return mapOf(
            "" to stringResource("date_system_default"),
            "MM/dd/yy" to "MM/dd/yy",
            "dd/MM/yy" to "dd/MM/yy",
            "yyyy-MM-dd" to "yyyy-MM-dd"
        ).mapValues { "${it.value} (${getFormattedDate(it.key)})" }
    }

    @Composable
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
fun SettingsGeneralScreen(menuController: MenuController) {
    val vm = viewModel<SettingsGeneralViewModel>()
    Column {
        Toolbar(stringResource("settings_general_screen"), menuController, closable = true)
        LazyColumn {
            item {
                ChoicePreference(
                    preference = vm.startScreen,
                    title = stringResource("start_screen"),
                    choices = vm.getStartScreenChoices()
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
