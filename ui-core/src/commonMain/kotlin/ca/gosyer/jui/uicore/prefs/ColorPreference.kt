/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.uicore.prefs

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import ca.gosyer.jui.core.prefs.Preference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class ColorPreference(
    private val preference: Preference<Int>,
) : Preference<Color> {
    override fun key(): String = preference.key()

    override fun get(): Color =
        if (isSet()) {
            Color(preference.get())
        } else {
            Color.Unspecified
        }

    override fun set(value: Color) {
        if (value != Color.Unspecified) {
            preference.set(value.toArgb())
        } else {
            preference.delete()
        }
    }

    override fun isSet(): Boolean = preference.isSet()

    override fun delete() {
        preference.delete()
    }

    override fun defaultValue(): Color = Color.Unspecified

    override fun changes(): Flow<Color> =
        preference.changes()
            .map { get() }

    override fun stateIn(scope: CoroutineScope): StateFlow<Color> =
        preference.changes().map {
            get()
        }.stateIn(scope, SharingStarted.Eagerly, get())
}

fun Preference<Int>.asColor(): ColorPreference = ColorPreference(this)
