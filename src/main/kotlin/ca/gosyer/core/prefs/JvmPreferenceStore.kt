/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.core.prefs

import ca.gosyer.common.prefs.Preference
import ca.gosyer.common.prefs.PreferenceStore
import com.russhwolf.settings.ObservableSettings
import kotlinx.serialization.KSerializer
import kotlinx.serialization.modules.SerializersModule

class JvmPreferenceStore(private val preferences: ObservableSettings) : PreferenceStore {

    /**
     * Returns an [String] preference for this [key].
     */
    override fun getString(key: String, defaultValue: String): Preference<String> {
        return JvmPreference(preferences, key, defaultValue, StringAdapter)
    }

    /**
     * Returns a [Long] preference for this [key].
     */
    override fun getLong(key: String, defaultValue: Long): Preference<Long> {
        return JvmPreference(preferences, key, defaultValue, LongAdapter)
    }

    /**
     * Returns an [Int] preference for this [key].
     */
    override fun getInt(key: String, defaultValue: Int): Preference<Int> {
        return JvmPreference(preferences, key, defaultValue, IntAdapter)
    }

    /**
     * Returns a [Float] preference for this [key].
     */
    override fun getFloat(key: String, defaultValue: Float): Preference<Float> {
        return JvmPreference(preferences, key, defaultValue, FloatAdapter)
    }

    /**
     * Returns a [Boolean] preference for this [key].
     */
    override fun getBoolean(key: String, defaultValue: Boolean): Preference<Boolean> {
        return JvmPreference(preferences, key, defaultValue, BooleanAdapter)
    }

    /**
     * Returns a [Set<String>] preference for this [key].
     */
    override fun getStringSet(key: String, defaultValue: Set<String>): Preference<Set<String>> {
        return JvmPreference(preferences, key, defaultValue, StringSetAdapter)
    }

    /**
     * Returns preference of type [T] for this [key]. The [serializer] and [deserializer] function
     * must be provided.
     */
    override fun <T> getObject(
        key: String,
        defaultValue: T,
        serializer: (T) -> String,
        deserializer: (String) -> T
    ): Preference<T> {
        val adapter = ObjectAdapter(serializer, deserializer)
        return JvmPreference(preferences, key, defaultValue, adapter)
    }

    /**
     * Returns preference of type [T] for this [key]. The [serializer] must be provided.
     */
    override fun <T> getJsonObject(
        key: String,
        defaultValue: T,
        serializer: KSerializer<T>,
        serializersModule: SerializersModule
    ): Preference<T> {
        val adapter = JsonObjectAdapter(defaultValue, serializer, serializersModule)
        return JvmPreference(preferences, key, defaultValue, adapter)
    }
}
