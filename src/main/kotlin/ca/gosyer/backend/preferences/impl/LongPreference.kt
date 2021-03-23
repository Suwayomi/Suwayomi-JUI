/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.backend.preferences.impl

import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.coroutines.getLongFlow
import com.russhwolf.settings.coroutines.getLongOrNullFlow
import com.russhwolf.settings.set

class LongPreference(
    override val settings: ObservableSettings,
    override val key: String,
    override val default: Long
): DefaultPreference<Long> {
    override fun get() = settings.getLong(key, default)
    override fun asFLow() = settings.getLongFlow(key, default)
    override fun set(value: Long) {
        settings[key] = value
    }
}

class LongNullPreference(
    override val settings: ObservableSettings,
    override val key: String,
): NullPreference<Long> {
    override fun get() = settings.getLongOrNull(key)
    override fun asFLow() = settings.getLongOrNullFlow(key)
    override fun set(value: Long?) {
        settings[key] = value
    }
}

fun ObservableSettings.getLongPreference(key: String, default: Long) = LongPreference(
    this,
    key,
    default
)

fun ObservableSettings.getLongPreference(key: String) = LongNullPreference(
    this,
    key
)