/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.domain.ui.service

import ca.gosyer.jui.core.prefs.Preference
import ca.gosyer.jui.core.prefs.PreferenceStore
import ca.gosyer.jui.domain.ui.model.StartScreen
import ca.gosyer.jui.domain.ui.model.ThemeMode
import ca.gosyer.jui.domain.ui.model.WindowSettings

class UiPreferences(
    private val preferenceStore: PreferenceStore,
) {
    fun themeMode(): Preference<ThemeMode> = preferenceStore.getJsonObject("theme_mode", ThemeMode.System, ThemeMode.serializer())

    fun lightTheme(): Preference<Int> = preferenceStore.getInt("theme_light", 0)

    fun darkTheme(): Preference<Int> = preferenceStore.getInt("theme_dark", 0)

    fun colorPrimaryLight(): Preference<Int> = preferenceStore.getInt("color_primary_light", 0)

    fun colorPrimaryDark(): Preference<Int> = preferenceStore.getInt("color_primary_dark", 0)

    fun colorSecondaryLight(): Preference<Int> = preferenceStore.getInt("color_secondary_light", 0)

    fun colorSecondaryDark(): Preference<Int> = preferenceStore.getInt("color_secondary_dark", 0)

    fun colorTertiaryLight(): Preference<Int> = preferenceStore.getInt("color_tertiary_light", 0)

    fun colorTertiaryDark(): Preference<Int> = preferenceStore.getInt("color_tertiary_dark", 0)

    fun startScreen(): Preference<StartScreen> =
        preferenceStore.getJsonObject(
            "start_screen",
            StartScreen.Library,
            StartScreen.serializer(),
        )

    fun confirmExit(): Preference<Boolean> = preferenceStore.getBoolean("confirm_exit", false)

    fun language(): Preference<String> = preferenceStore.getString("language", "")

    fun dateFormat(): Preference<String> = preferenceStore.getString("date_format", "")

    fun window(): Preference<WindowSettings> = preferenceStore.getJsonObject("window", WindowSettings(), WindowSettings.serializer())

    fun readerWindow(): Preference<WindowSettings> =
        preferenceStore.getJsonObject(
            "reader_window",
            WindowSettings(),
            WindowSettings.serializer(),
        )

    fun windowDecorations(): Preference<Boolean> = preferenceStore.getBoolean("window_decorations", true)
}
