/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.core.prefs

import com.russhwolf.settings.ObservableSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

internal class StandardPreference<T>(
    private val preferences: ObservableSettings,
    private val key: String,
    private val defaultValue: T,
    private val adapter: Adapter<T>,
) : Preference<T> {
    /**
     * Returns the key of this preference.
     */
    override fun key(): String = key

    /**
     * Returns the current value of this preference.
     */
    override fun get(): T =
        if (isSet()) {
            adapter.get(key, preferences)
        } else {
            defaultValue
        }

    /**
     * Sets a new [value] for this preference.
     */
    @OptIn(DelicateCoroutinesApi::class)
    override fun set(value: T) {
        adapter.set(key, value, preferences)
        GlobalScope.launch {
            listener.emit(key)
        }
    }

    /**
     * Returns whether there's an existing entry for this preference.
     */
    override fun isSet(): Boolean = adapter.isSet(preferences.keys, key)

    /**
     * Deletes the entry of this preference.
     */
    override fun delete() {
        preferences.remove(key)
    }

    /**
     * Returns the default value of this preference
     */
    override fun defaultValue(): T = defaultValue

    /**
     * Returns a cold [Flow] of this preference to receive updates when its value changes.
     */
    override fun changes(): Flow<T> = listener
        .filter { it == key }
        .map { get() }

    /**
     * Returns a hot [StateFlow] of this preference bound to the given [scope], allowing to read the
     * current value and receive preference updates.
     */
    override fun stateIn(scope: CoroutineScope): StateFlow<T> = changes().stateIn(scope, SharingStarted.Eagerly, get())

    companion object {
        private val listener = MutableSharedFlow<String>()
    }
}
