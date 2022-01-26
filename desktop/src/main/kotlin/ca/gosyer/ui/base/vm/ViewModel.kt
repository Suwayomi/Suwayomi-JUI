/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.base.vm

import ca.gosyer.common.prefs.Preference
import ca.gosyer.ui.base.prefs.PreferenceMutableStateFlow
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

abstract class ViewModel {

    protected val scope = MainScope()

    fun destroy() {
        scope.cancel()
        onDestroy()
    }

    open fun onDestroy() {}

    fun <T> Preference<T>.asStateFlow() = PreferenceMutableStateFlow(this, scope)

    fun <T> Flow<T>.asStateFlow(initialValue: T): StateFlow<T> {
        val state = MutableStateFlow(initialValue)
        scope.launch {
            collect { state.value = it }
        }
        return state
    }
}
