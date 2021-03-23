/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.backend.preferences.impl

import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.Settings
import com.russhwolf.settings.get
import com.russhwolf.settings.serialization.decodeValue
import com.russhwolf.settings.serialization.decodeValueOrNull
import com.russhwolf.settings.serialization.encodeValue
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.serialization.KSerializer

class JsonPreference<T>(
    override val settings: ObservableSettings,
    override val key: String,
    override val default: T,
    private val serializer: KSerializer<T>
): DefaultPreference<T> {
    override fun get() = settings.decodeValue(serializer, key, default)
    override fun asFLow() = settings.createFlow(key, default) { key, default ->
        decodeValue(serializer, key, default)
    }
    override fun set(value: T) {
        settings.encodeValue(serializer, key, value)
    }

    fun getJson(): String? {
        return settings[key]
    }
}

class JsonNullPreference<T>(
    override val settings: ObservableSettings,
    override val key: String,
    private val serializer: KSerializer<T>
): NullPreference<T> {
    override fun get() = settings.decodeValueOrNull(serializer, key)
    override fun asFLow() = settings.createFlow<T?>(key, null) { key, _ ->
        decodeValueOrNull(serializer, key)
    }
    override fun set(value: T?) {
        if (value != null) {
            settings.encodeValue(serializer, key, value)
        } else {
            settings.remove(key)
        }
    }

    fun getJson(): String? {
        return settings[key]
    }
}

fun <T> ObservableSettings.getJsonPreference(key: String, default: T, serializer: KSerializer<T>) = JsonPreference(
    this,
    key,
    default,
    serializer
)

fun <T> ObservableSettings.getJsonPreference(key: String, serializer: KSerializer<T>) = JsonNullPreference(
    this,
    key,
    serializer
)

private inline fun <T> ObservableSettings.createFlow(
    key: String,
    defaultValue: T,
    crossinline getter: Settings.(String, T) -> T
): Flow<T> = callbackFlow {
    offer(getter(key, defaultValue))
    val listener = addListener(key) {
        offer(getter(key, defaultValue))
    }
    awaitClose {
        listener.deactivate()
    }
}