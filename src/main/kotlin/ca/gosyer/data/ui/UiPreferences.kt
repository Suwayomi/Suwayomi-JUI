/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.data.ui

import ca.gosyer.common.prefs.Preference
import ca.gosyer.common.prefs.PreferenceStore
import ca.gosyer.data.ui.model.StartScreen
import ca.gosyer.data.ui.model.ThemeMode
import ca.gosyer.data.ui.model.WindowSettings
import kotlinx.serialization.builtins.serializer

class UiPreferences(private val preferenceStore: PreferenceStore) {

    fun themeMode(): Preference<ThemeMode> {
        return preferenceStore.getJsonObject("theme_mode", ThemeMode.System, ThemeMode.serializer())
    }

    fun lightTheme(): Preference<Int> {
        return preferenceStore.getInt("theme_light", 0)
    }

    fun darkTheme(): Preference<Int> {
        return preferenceStore.getInt("theme_dark", 0)
    }

    fun colorPrimaryLight(): Preference<Int> {
        return preferenceStore.getInt("color_primary_light", 0)
    }

    fun colorPrimaryDark(): Preference<Int> {
        return preferenceStore.getInt("color_primary_dark", 0)
    }

    fun colorSecondaryLight(): Preference<Int> {
        return preferenceStore.getInt("color_secondary_light", 0)
    }

    fun colorSecondaryDark(): Preference<Int> {
        return preferenceStore.getInt("color_secondary_dark", 0)
    }

    fun startScreen(): Preference<StartScreen> {
        return preferenceStore.getJsonObject("start_screen", StartScreen.Library, StartScreen.serializer())
    }

    fun confirmExit(): Preference<Boolean> {
        return preferenceStore.getBoolean("confirm_exit", false)
    }

    fun language(): Preference<String> {
        return preferenceStore.getString("language", "")
    }

    fun dateFormat(): Preference<String> {
        return preferenceStore.getString("date_format", "")
    }

    fun window(): Preference<WindowSettings> {
        return preferenceStore.getJsonObject("window", WindowSettings(), WindowSettings.serializer())
    }

    fun readerWindow(): Preference<WindowSettings> {
        return preferenceStore.getJsonObject("reader_window", WindowSettings(), WindowSettings.serializer())
    }

    fun windowDecorations(): Preference<Boolean> {
        return preferenceStore.getBoolean("window_decorations", true)
    }
}
