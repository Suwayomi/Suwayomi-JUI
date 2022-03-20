/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Divider
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ca.gosyer.jui.core.lang.getDefault
import ca.gosyer.jui.core.lang.getDisplayName
import ca.gosyer.jui.data.ui.UiPreferences
import ca.gosyer.jui.data.ui.model.StartScreen
import ca.gosyer.jui.i18n.MR
import ca.gosyer.jui.ui.base.navigation.Toolbar
import ca.gosyer.jui.ui.base.prefs.ChoicePreference
import ca.gosyer.jui.ui.base.prefs.SwitchPreference
import ca.gosyer.jui.uicore.components.VerticalScrollbar
import ca.gosyer.jui.uicore.components.rememberScrollbarAdapter
import ca.gosyer.jui.uicore.prefs.PreferenceMutableStateFlow
import ca.gosyer.jui.uicore.resources.rememberReadText
import ca.gosyer.jui.uicore.resources.stringResource
import ca.gosyer.jui.uicore.vm.ContextWrapper
import ca.gosyer.jui.uicore.vm.ViewModel
import ca.gosyer.jui.uicore.vm.viewModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import cafe.adriel.voyager.core.screen.uniqueScreenKey
import com.soywiz.klock.DateTime
import com.soywiz.klock.KlockLocale
import com.soywiz.klock.PatternDateFormat
import com.soywiz.klock.format
import io.fluidsonic.locale.Locale
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import me.tatarka.inject.annotations.Inject

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
    contextWrapper: ContextWrapper
) : ViewModel(contextWrapper) {

    val startScreen = uiPreferences.startScreen().asStateFlow()
    val confirmExit = uiPreferences.confirmExit().asStateFlow()
    val language = uiPreferences.language().asStateFlow()
    val dateFormat = uiPreferences.dateFormat().asStateFlow()

    private val now = DateTime.now()
    private val currentLocale = Locale.getDefault()

    @Composable
    fun getStartScreenChoices() = mapOf(
        StartScreen.Library to stringResource(MR.strings.location_library),
        StartScreen.Updates to stringResource(MR.strings.location_updates),
        StartScreen.Sources to stringResource(MR.strings.location_sources),
        StartScreen.Extensions to stringResource(MR.strings.location_extensions)
    )

    @Composable
    fun getLanguageChoices(): Map<String, String> {
        val langJson = MR.files.languages.rememberReadText()
        val langs by derivedStateOf {
            Json.decodeFromString<JsonObject>(langJson)["langs"]!!
                .jsonArray
                .map { it.jsonPrimitive.content }
                .associateWith { Locale.forLanguageTag(it).getDisplayName(currentLocale) }
        }
        return mapOf("" to stringResource(MR.strings.language_system_default, currentLocale.getDisplayName(currentLocale)))
            .plus(langs)
            .toMap()
    }

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
            "" -> KlockLocale.default.formatDateShort
            else -> PatternDateFormat(prefValue)
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
