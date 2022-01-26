/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.base.prefs

import ca.gosyer.core.prefs.Preference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class PreferenceMutableStateFlow<T>(
    private val preference: Preference<T>,
    scope: CoroutineScope,
    private val state: MutableStateFlow<T> = MutableStateFlow(preference.get())
) : MutableStateFlow<T> by state {

    init {
        preference.changes()
            .onEach { state.value = it }
            .launchIn(scope)
    }

    override var value: T
        get() = state.value
        set(value) {
            preference.set(value)
        }
}

fun <T> Preference<T>.asStateIn(scope: CoroutineScope): PreferenceMutableStateFlow<T> {
    return PreferenceMutableStateFlow(this, scope)
}
