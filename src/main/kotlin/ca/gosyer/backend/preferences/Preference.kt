/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.backend.preferences

import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import ca.gosyer.backend.preferences.impl.getBooleanPreference
import ca.gosyer.backend.preferences.impl.getJsonPreference
import ca.gosyer.backend.preferences.impl.getLongPreference
import ca.gosyer.backend.preferences.impl.getStringPreference
import ca.gosyer.ui.library.DisplayMode
import ca.gosyer.util.compose.color
import com.russhwolf.settings.JvmPreferencesSettings
import com.russhwolf.settings.ObservableSettings
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import org.koin.dsl.module
import java.util.prefs.Preferences

class PreferenceHelper {
    private val settings = JvmPreferencesSettings(Preferences.userRoot()) as ObservableSettings
    val serverUrl = settings.getStringPreference("server_url", "http://localhost:4567")
    val enabledLangs = settings.getJsonPreference("server_langs", listOf("all", "en"), ListSerializer(String.serializer()))
    val libraryDisplay = settings.getJsonPreference("library_display", DisplayMode.CompactGrid, DisplayMode.serializer())

    val lightTheme = settings.getBooleanPreference("light_theme", true)
    val lightPrimary = settings.getLongPreference("light_color_primary", 0xFF00a2ff)
    val lightPrimaryVariant = settings.getLongPreference("light_color_primary_variant", 0xFF0091EA)
    val lightSecondary = settings.getLongPreference("light_color_secondary", 0xFFF44336)
    val lightSecondaryVaraint = settings.getLongPreference("light_color_secondary_variant", 0xFFE53935)

    fun getTheme() = when (lightTheme.get()) {
        true -> lightColors(
            lightPrimary.get().color,
            lightPrimaryVariant.get().color,
            lightSecondary.get().color,
            lightSecondaryVaraint.get().color
        )
        false -> darkColors()
    }
}

val preferencesModule = module {
    single { PreferenceHelper() }
}