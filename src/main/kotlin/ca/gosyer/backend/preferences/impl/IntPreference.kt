/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.backend.preferences.impl

import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.coroutines.getIntFlow
import com.russhwolf.settings.coroutines.getIntOrNullFlow
import com.russhwolf.settings.set

class IntPreference(
    override val settings: ObservableSettings,
    override val key: String,
    override val default: Int
): DefaultPreference<Int> {
    override fun get() = settings.getInt(key, default)
    override fun asFLow() = settings.getIntFlow(key, default)
    override fun set(value: Int) {
        settings[key] = value
    }
}

class IntNullPreference(
    override val settings: ObservableSettings,
    override val key: String,
): NullPreference<Int> {
    override fun get() = settings.getIntOrNull(key)
    override fun asFLow() = settings.getIntOrNullFlow(key)
    override fun set(value: Int?) {
        settings[key] = value
    }
}

fun ObservableSettings.getIntPreference(key: String, default: Int) = IntPreference(
    this,
    key,
    default
)

fun ObservableSettings.getIntPreference(key: String) = IntNullPreference(
    this,
    key
)