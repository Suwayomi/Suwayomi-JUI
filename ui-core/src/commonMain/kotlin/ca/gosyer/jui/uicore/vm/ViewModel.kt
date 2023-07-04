/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.uicore.vm

import ca.gosyer.jui.core.lang.launchUI
import ca.gosyer.jui.core.prefs.Preference
import ca.gosyer.jui.uicore.prefs.PreferenceMutableStateFlow
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.coroutineScope
import dev.icerock.moko.resources.StringResource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

abstract class ViewModel(private val contextWrapper: ContextWrapper) : ScreenModel {
    protected open val scope: CoroutineScope
        get() = coroutineScope

    fun <T> Preference<T>.asStateFlow() = PreferenceMutableStateFlow(this, scope)

    fun <T> Flow<T>.asStateFlow(initialValue: T): StateFlow<T> {
        val state = MutableStateFlow(initialValue)
        scope.launch {
            collect { state.value = it }
        }
        return state
    }

    fun StringResource.toPlatformString(): String {
        return contextWrapper.toPlatformString(this)
    }
    fun StringResource.toPlatformString(vararg args: Any): String {
        return contextWrapper.toPlatformString(this, *args)
    }
    fun toast(
        string: String,
        length: Length = Length.SHORT,
    ) {
        scope.launchUI {
            contextWrapper.toast(string, length)
        }
    }

    @Suppress("RedundantOverride") // So classes that inherit ViewModel can see it
    override fun onDispose() {
        super.onDispose()
    }
}
