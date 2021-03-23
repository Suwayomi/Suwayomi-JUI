/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.backend.preferences.impl

import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.coroutines.getBooleanFlow
import com.russhwolf.settings.coroutines.getBooleanOrNullFlow
import com.russhwolf.settings.set

class BooleanPreference(
    override val settings: ObservableSettings,
    override val key: String,
    override val default: Boolean
): DefaultPreference<Boolean> {
    override fun get() = settings.getBoolean(key, default)
    override fun asFLow() = settings.getBooleanFlow(key, default)
    override fun set(value: Boolean) {
        settings[key] = value
    }
}

class BooleanNullPreference(
    override val settings: ObservableSettings,
    override val key: String,
): NullPreference<Boolean> {
    override fun get() = settings.getBooleanOrNull(key)
    override fun asFLow() = settings.getBooleanOrNullFlow(key)
    override fun set(value: Boolean?) {
        settings[key] = value
    }
}

fun ObservableSettings.getBooleanPreference(key: String, default: Boolean) = BooleanPreference(
    this,
    key,
    default
)

fun ObservableSettings.getBooleanPreference(key: String) = BooleanNullPreference(
    this,
    key
)