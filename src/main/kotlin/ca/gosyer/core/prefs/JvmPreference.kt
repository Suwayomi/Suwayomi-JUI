/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.core.prefs

import ca.gosyer.common.prefs.Preference
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.contains
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.stateIn

internal class JvmPreference<T>(
    private val preferences: ObservableSettings,
    private val key: String,
    private val defaultValue: T,
    private val adapter: Adapter<T>
) : Preference<T> {

    interface Adapter<T> {
        fun get(key: String, preferences: ObservableSettings): T

        fun set(key: String, value: T, editor: ObservableSettings)
    }

    /**
     * Returns the key of this preference.
     */
    override fun key(): String {
        return key
    }

    /**
     * Returns the current value of this preference.
     */
    override fun get(): T {
        return if (!preferences.contains(key)) {
            defaultValue
        } else {
            adapter.get(key, preferences)
        }
    }

    /**
     * Sets a new [value] for this preference.
     */
    override fun set(value: T) {
        adapter.set(key, value, preferences)
    }

    /**
     * Returns whether there's an existing entry for this preference.
     */
    override fun isSet(): Boolean {
        return preferences.contains(key)
    }

    /**
     * Deletes the entry of this preference.
     */
    override fun delete() {
        preferences.remove(key)
    }

    /**
     * Returns the default value of this preference
     */
    override fun defaultValue(): T {
        return defaultValue
    }

    /**
     * Returns a cold [Flow] of this preference to receive updates when its value changes.
     */
    override fun changes(): Flow<T> {
        return callbackFlow {
            val listener = preferences.addListener(key) {
                offer(get())
            }
            awaitClose { listener.deactivate() }
        }
    }

    /**
     * Returns a hot [StateFlow] of this preference bound to the given [scope], allowing to read the
     * current value and receive preference updates.
     */
    override fun stateIn(scope: CoroutineScope): StateFlow<T> {
        return changes().stateIn(scope, SharingStarted.Eagerly, get())
    }
}
