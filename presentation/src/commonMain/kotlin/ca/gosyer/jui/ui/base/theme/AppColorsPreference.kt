/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.base.theme

import androidx.compose.ui.graphics.Color
import ca.gosyer.jui.core.prefs.Preference
import ca.gosyer.jui.data.ui.UiPreferences
import ca.gosyer.jui.uicore.prefs.PreferenceMutableStateFlow
import ca.gosyer.jui.uicore.prefs.asColor
import ca.gosyer.jui.uicore.prefs.asStateIn
import kotlinx.coroutines.CoroutineScope

data class AppColorsPreference(
    val primary: Preference<Color>,
    val secondary: Preference<Color>
)

class AppColorsPreferenceState(
    val primaryStateFlow: PreferenceMutableStateFlow<Color>,
    val secondaryStateFlow: PreferenceMutableStateFlow<Color>
)

fun UiPreferences.getLightColors(): AppColorsPreference {
    return AppColorsPreference(
        colorPrimaryLight().asColor(),
        colorSecondaryLight().asColor()
    )
}

fun UiPreferences.getDarkColors(): AppColorsPreference {
    return AppColorsPreference(
        colorPrimaryDark().asColor(),
        colorSecondaryDark().asColor()
    )
}

fun AppColorsPreference.asStateFlow(scope: CoroutineScope): AppColorsPreferenceState {
    return AppColorsPreferenceState(
        primary.asStateIn(scope),
        secondary.asStateIn(scope)
    )
}
