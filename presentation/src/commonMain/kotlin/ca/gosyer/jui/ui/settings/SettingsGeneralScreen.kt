/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Divider
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.intl.Locale
import ca.gosyer.jui.core.lang.getDisplayName
import ca.gosyer.jui.core.lang.withIOContext
import ca.gosyer.jui.data.base.DateHandler
import ca.gosyer.jui.domain.ui.model.StartScreen
import ca.gosyer.jui.domain.ui.service.UiPreferences
import ca.gosyer.jui.i18n.MR
import ca.gosyer.jui.ui.base.navigation.Toolbar
import ca.gosyer.jui.ui.base.prefs.ChoicePreference
import ca.gosyer.jui.ui.base.prefs.SwitchPreference
import ca.gosyer.jui.ui.main.components.bottomNav
import ca.gosyer.jui.ui.viewModel
import ca.gosyer.jui.uicore.components.VerticalScrollbar
import ca.gosyer.jui.uicore.components.rememberScrollbarAdapter
import ca.gosyer.jui.uicore.components.scrollbarPadding
import ca.gosyer.jui.uicore.insets.navigationBars
import ca.gosyer.jui.uicore.insets.statusBars
import ca.gosyer.jui.uicore.prefs.PreferenceMutableStateFlow
import ca.gosyer.jui.uicore.resources.readTextAsync
import ca.gosyer.jui.uicore.resources.stringResource
import ca.gosyer.jui.uicore.vm.ContextWrapper
import ca.gosyer.jui.uicore.vm.ViewModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import cafe.adriel.voyager.core.screen.uniqueScreenKey
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.datetime.Clock
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
        val vm = viewModel { settingsGeneralViewModel() }
        SettingsGeneralScreenContent(
            startScreen = vm.startScreen,
            startScreenChoices = vm.getStartScreenChoices(),
            confirmExit = vm.confirmExit,
            language = vm.language,
            languageChoices = vm.getLanguageChoices(),
            dateFormat = vm.dateFormat,
            dateFormatChoices = vm.getDateChoices(),
        )
    }
}

class SettingsGeneralViewModel @Inject constructor(
    private val dateHandler: DateHandler,
    uiPreferences: UiPreferences,
    contextWrapper: ContextWrapper,
) : ViewModel(contextWrapper) {

    val startScreen = uiPreferences.startScreen().asStateFlow()
    val confirmExit = uiPreferences.confirmExit().asStateFlow()
    val language = uiPreferences.language().asStateFlow()
    val dateFormat = uiPreferences.dateFormat().asStateFlow()

    private val now = Clock.System.now()
    private val currentLocale = Locale.current

    @Composable
    fun getStartScreenChoices(): ImmutableMap<StartScreen, String> = persistentMapOf(
        StartScreen.Library to stringResource(MR.strings.location_library),
        StartScreen.Updates to stringResource(MR.strings.location_updates),
        StartScreen.Sources to stringResource(MR.strings.location_sources),
        StartScreen.Extensions to stringResource(MR.strings.location_extensions),
    )

    @Composable
    fun getLanguageChoices(): ImmutableMap<String, String> {
        val langJsonState = MR.files.languages.readTextAsync()
        val langs by produceState(emptyMap(), langJsonState.value) {
            val langJson = langJsonState.value
            if (langJson != null) {
                withIOContext {
                    value = Json.decodeFromString<JsonObject>(langJson)["langs"]
                        ?.jsonArray
                        .orEmpty()
                        .map { it.jsonPrimitive.content }
                        .associateWith { Locale(it).getDisplayName(currentLocale) }
                }
            }
        }
        return mapOf("" to stringResource(MR.strings.language_system_default, currentLocale.getDisplayName(currentLocale)))
            .plus(langs)
            .toImmutableMap()
    }

    @Composable
    fun getDateChoices(): ImmutableMap<String, String> {
        return dateHandler.formatOptions
            .associateWith {
                it.ifEmpty { stringResource(MR.strings.date_system_default) } +
                    " (${getFormattedDate(it)})"
            }
            .toImmutableMap()
    }

    @Composable
    private fun getFormattedDate(prefValue: String): String {
        return dateHandler.getDateFormat(prefValue).invoke(now)
    }
}

@Composable
fun SettingsGeneralScreenContent(
    startScreen: PreferenceMutableStateFlow<StartScreen>,
    startScreenChoices: ImmutableMap<StartScreen, String>,
    confirmExit: PreferenceMutableStateFlow<Boolean>,
    language: PreferenceMutableStateFlow<String>,
    languageChoices: ImmutableMap<String, String>,
    dateFormat: PreferenceMutableStateFlow<String>,
    dateFormatChoices: ImmutableMap<String, String>,
) {
    Scaffold(
        modifier = Modifier.windowInsetsPadding(
            WindowInsets.statusBars.add(
                WindowInsets.navigationBars.only(WindowInsetsSides.Horizontal),
            ),
        ),
        topBar = {
            Toolbar(stringResource(MR.strings.settings_general_screen))
        },
    ) {
        Box(Modifier.padding(it)) {
            val state = rememberLazyListState()
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = state,
                contentPadding = WindowInsets.bottomNav.add(
                    WindowInsets.navigationBars.only(
                        WindowInsetsSides.Bottom,
                    ),
                ).asPaddingValues(),
            ) {
                item {
                    ChoicePreference(
                        preference = startScreen,
                        title = stringResource(MR.strings.start_screen),
                        choices = startScreenChoices,
                    )
                }
                item {
                    SwitchPreference(
                        preference = confirmExit,
                        title = stringResource(MR.strings.confirm_exit),
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
                        choices = dateFormatChoices,
                    )
                }
            }
            VerticalScrollbar(
                rememberScrollbarAdapter(state),
                Modifier.align(Alignment.CenterEnd)
                    .fillMaxHeight()
                    .scrollbarPadding()
                    .windowInsetsPadding(
                        WindowInsets.bottomNav.add(
                            WindowInsets.navigationBars.only(
                                WindowInsetsSides.Bottom,
                            ),
                        ),
                    ),
            )
        }
    }
}
