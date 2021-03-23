/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.backend.preferences.impl

import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.coroutines.getDoubleFlow
import com.russhwolf.settings.coroutines.getDoubleOrNullFlow
import com.russhwolf.settings.set

class DoublePreference(
    override val settings: ObservableSettings,
    override val key: String,
    override val default: Double
): DefaultPreference<Double> {
    override fun get() = settings.getDouble(key, default)
    override fun asFLow() = settings.getDoubleFlow(key, default)
    override fun set(value: Double) {
        settings[key] = value
    }
}

class DoubleNullPreference(
    override val settings: ObservableSettings,
    override val key: String,
): NullPreference<Double> {
    override fun get() = settings.getDoubleOrNull(key)
    override fun asFLow() = settings.getDoubleOrNullFlow(key)
    override fun set(value: Double?) {
        settings[key] = value
    }
}

fun ObservableSettings.getDoublePreference(key: String, default: Double) = DoublePreference(
    this,
    key,
    default
)

fun ObservableSettings.getDoublePreference(key: String) = DoubleNullPreference(
    this,
    key
)