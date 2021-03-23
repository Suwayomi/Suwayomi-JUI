/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.backend.preferences.impl

import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.coroutines.getFloatFlow
import com.russhwolf.settings.coroutines.getFloatOrNullFlow
import com.russhwolf.settings.set

class FloatPreference(
    override val settings: ObservableSettings,
    override val key: String,
    override val default: Float
): DefaultPreference<Float> {
    override fun get() = settings.getFloat(key, default)
    override fun asFLow() = settings.getFloatFlow(key, default)
    override fun set(value: Float) {
        settings[key] = value
    }
}

class FloatNullPreference(
    override val settings: ObservableSettings,
    override val key: String,
): NullPreference<Float> {
    override fun get() = settings.getFloatOrNull(key)
    override fun asFLow() = settings.getFloatOrNullFlow(key)
    override fun set(value: Float?) {
        settings[key] = value
    }
}

fun ObservableSettings.getFloatPreference(key: String, default: Float) = FloatPreference(
    this,
    key,
    default
)

fun ObservableSettings.getFloatPreference(key: String) = FloatNullPreference(
    this,
    key
)