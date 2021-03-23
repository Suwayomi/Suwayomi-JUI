/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.backend.preferences.impl

import ca.gosyer.util.system.asStateFlow
import com.russhwolf.settings.ObservableSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface Preference {
    /**
     * The settings to watch for this preference. Must be a instance of [ObservableSettings]
     * so that we can watch changes
     */
    val settings: ObservableSettings

    /**
     * The key for this preference
     */
    val key: String
}


interface NullPreference <T>: Preference {
    /**
     * Returns the value stored at [key] as [T], or `null` if no value was stored. If a value of a different
     * type was stored at `key`, the behavior is not defined.
     */
    fun get(): T?

    /**
     * Create a new [Flow], based on observing the given [key]. This flow will immediately emit the
     * current value and then emit any subsequent values when the underlying `Settings` changes. When no value is present,
     * `null` will be emitted instead.
     */
    fun asFLow(): Flow<T?>

    /**
     * See [asFLow], this function is equilivent to that except in that it stores the latest value instead of emitting
     */
    fun asStateFlow(scope: CoroutineScope): StateFlow<T?> = asFLow().asStateFlow(get(), scope, true)

    /**
     * Stores a [T] value at [key], or remove what's there if [value] is null.
     */
    fun set(value: T?)
}

interface DefaultPreference <T>: Preference {
    val default: T

    /**
     * Returns the value stored at [key] as [T], or [default] if no value was stored. If a value of a different
     * type was stored at `key`, the behavior is not defined.
     */
    fun get(): T

    /**
     * Create a new [Flow], based on observing the given [key]. This flow will immediately emit the
     * current value and then emit any subsequent values when the underlying `Settings` changes. When no value is present,
     * [default] will be emitted instead.
     */
    fun asFLow(): Flow<T>

    /**
     * See [asFLow], this function is equilivent to that except in that it stores the latest value instead of emitting
     */
    fun asStateFlow(scope: CoroutineScope): StateFlow<T> = asFLow().asStateFlow(get(), scope, true)
    /**
     * Stores the [T] [value] at [key].
     */
    fun set(value: T)
}