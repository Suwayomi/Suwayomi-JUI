/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.base.theme

import androidx.compose.ui.graphics.Color
import ca.gosyer.common.prefs.Preference
import ca.gosyer.data.ui.UiPreferences
import ca.gosyer.ui.base.prefs.asColor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

data class AppColorsPreference(
    val primary: Preference<Color>,
    val secondary: Preference<Color>
)

class AppColorsPreferenceState(
    val primaryStateFlow: StateFlow<Color>,
    val secondaryStateFlow: StateFlow<Color>
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

fun AppColorsPreference.asState(scope: CoroutineScope): AppColorsPreferenceState {
    return AppColorsPreferenceState(
        primary.stateIn(scope),
        secondary.stateIn(scope)
    )
}
