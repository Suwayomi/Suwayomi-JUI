/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.settings

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.Divider
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ca.gosyer.data.ui.UiPreferences
import ca.gosyer.data.ui.model.StartScreen
import ca.gosyer.i18n.MR
import ca.gosyer.ui.base.navigation.Toolbar
import ca.gosyer.ui.base.prefs.ChoicePreference
import ca.gosyer.ui.base.prefs.SwitchPreference
import ca.gosyer.uicore.prefs.PreferenceMutableStateFlow
import ca.gosyer.uicore.resources.stringResource
import ca.gosyer.uicore.vm.ViewModel
import ca.gosyer.uicore.vm.viewModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import cafe.adriel.voyager.core.screen.uniqueScreenKey
import me.tatarka.inject.annotations.Inject
import okio.Path.Companion.toPath
import okio.asResourceFileSystem
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

class SettingsGeneralScreen : Screen {
    override val key: ScreenKey = uniqueScreenKey

    @Composable
    override fun Content() {
        val vm = viewModel<SettingsGeneralViewModel>()
        SettingsGeneralScreenContent(
            startScreen = vm.startScreen,
            startScreenChoices = vm.getStartScreenChoices(),
            confirmExit = vm.confirmExit,
            language = vm.language,
            languageChoices = vm.getLanguageChoices(),
            dateFormat = vm.dateFormat,
            dateFormatChoices = vm.getDateChoices()
        )
    }
}

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
        StartScreen.Library to stringResource(MR.strings.location_library),
        StartScreen.Updates to stringResource(MR.strings.location_updates),
        StartScreen.Sources to stringResource(MR.strings.location_sources),
        StartScreen.Extensions to stringResource(MR.strings.location_extensions)
    )

    @Composable
    fun getLanguageChoices(): Map<String, String> = (
        mapOf(
            "" to stringResource(MR.strings.language_system_default, currentLocale.getDisplayName(currentLocale))
        ) + this::class.java.classLoader.asResourceFileSystem().list("/localization/".toPath())
            .asSequence()
            .drop(1)
            .map { it.name.substringBeforeLast('.') }
            .map { it.substringAfter("mokoBundle_") }
            .map(String::trim)
            .map { it.replace("-r", "-") }
            .filterNot(String::isBlank)
            .associateWith { Locale.forLanguageTag(it).getDisplayName(currentLocale) }
        )
        .toSortedMap(compareBy { it.lowercase() })

    @Composable
    fun getDateChoices(): Map<String, String> {
        return mapOf(
            "" to stringResource(MR.strings.date_system_default),
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
fun SettingsGeneralScreenContent(
    startScreen: PreferenceMutableStateFlow<StartScreen>,
    startScreenChoices: Map<StartScreen, String>,
    confirmExit: PreferenceMutableStateFlow<Boolean>,
    language: PreferenceMutableStateFlow<String>,
    languageChoices: Map<String, String>,
    dateFormat: PreferenceMutableStateFlow<String>,
    dateFormatChoices: Map<String, String>
) {
    Scaffold(
        topBar = {
            Toolbar(stringResource(MR.strings.settings_general_screen))
        }
    ) {
        Box(Modifier.padding(it)) {
            val state = rememberLazyListState()
            LazyColumn(Modifier.fillMaxSize(), state) {
                item {
                    ChoicePreference(
                        preference = startScreen,
                        title = stringResource(MR.strings.start_screen),
                        choices = startScreenChoices
                    )
                }
                item {
                    SwitchPreference(
                        preference = confirmExit,
                        title = stringResource(MR.strings.confirm_exit)
                    )
                }
                item {
                    Divider()
                }
                item {
                    ChoicePreference(
                        preference = language,
                        title = stringResource(MR.strings.language),
                        choices = languageChoices,
                    )
                }
                item {
                    ChoicePreference(
                        preference = dateFormat,
                        title = stringResource(MR.strings.date_format),
                        choices = dateFormatChoices
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
