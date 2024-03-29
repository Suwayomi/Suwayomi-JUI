/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.base.state

import ca.gosyer.jui.uicore.vm.ViewModel
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.internal.SynchronizedObject
import kotlinx.coroutines.internal.synchronized
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

fun <T> SavedStateHandle.getStateFlow(initialValue: () -> T): SavedStateHandleDelegate<T> = SavedStateHandleDelegate(this, initialValue)

@OptIn(InternalCoroutinesApi::class)
class SavedStateHandleDelegate<T>(
    private val savedStateHandle: SavedStateHandle,
    private val initialValue: () -> T,
) : ReadOnlyProperty<ViewModel, SavedStateHandleStateFlow<T>> {
    private val synchronizedObject = SynchronizedObject()

    private var item: SavedStateHandleStateFlow<T>? = null

    override fun getValue(
        thisRef: ViewModel,
        property: KProperty<*>,
    ): SavedStateHandleStateFlow<T> =
        item ?: synchronized(synchronizedObject) {
            if (item == null) {
                savedStateHandle.getSavedStateFlow(property.name, initialValue)
                    .also { item = it }
            } else {
                item!!
            }
        }
}

class SavedStateHandleStateFlow<T>(
    private val key: String,
    private val savedStateHandle: SavedStateHandle,
    private val stateFlow: StateFlow<T>,
) : StateFlow<T> by stateFlow {
    override var value: T
        get() = stateFlow.value

        /**
         * May have to be called on the main thread if there is a livedata with the same [key]
         */
        set(value) = savedStateHandle.set(key, value)

    fun asStateFlow() = stateFlow
}

fun <T> SavedStateHandle.getSavedStateFlow(
    key: String,
    initialValue: () -> T,
): SavedStateHandleStateFlow<T> {
    val value = get<T>(key)

    val flow = if (value != null) {
        getStateFlow(key, value)
    } else {
        getStateFlow(key, initialValue())
    }

    return SavedStateHandleStateFlow(
        key = key,
        savedStateHandle = this,
        stateFlow = flow,
    )
}
