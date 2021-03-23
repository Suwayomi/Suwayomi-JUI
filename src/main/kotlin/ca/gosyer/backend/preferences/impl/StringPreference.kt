/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.backend.preferences.impl

import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.coroutines.getStringFlow
import com.russhwolf.settings.coroutines.getStringOrNullFlow
import com.russhwolf.settings.set

class StringPreference(
    override val settings: ObservableSettings,
    override val key: String,
    override val default: String
): DefaultPreference<String> {
    override fun get() = settings.getString(key, default)
    override fun asFLow() = settings.getStringFlow(key, default)
    override fun set(value: String) {
        settings[key] = value
    }
}

class StringNullPreference(
    override val settings: ObservableSettings,
    override val key: String,
): NullPreference<String> {
    override fun get() = settings.getStringOrNull(key)
    override fun asFLow() = settings.getStringOrNullFlow(key)
    override fun set(value: String?) {
        settings[key] = value
    }
}

fun ObservableSettings.getStringPreference(key: String, default: String) = StringPreference(
    this,
    key,
    default
)

fun ObservableSettings.getStringPreference(key: String) = StringNullPreference(
    this,
    key
)