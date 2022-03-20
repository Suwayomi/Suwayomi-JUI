/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.core.prefs

import kotlinx.serialization.KSerializer
import kotlinx.serialization.modules.SerializersModule

/**
 * An implementation of a [PreferenceStore] which is initialized on first access. Useful when
 * providing preference instances to classes that may not use them at all.
 */
class LazyPreferenceStore(
    private val lazyStore: Lazy<PreferenceStore>
) : PreferenceStore {

    /**
     * Returns an [String] preference for this [key].
     */
    override fun getString(key: String, defaultValue: String): Preference<String> {
        return lazyStore.value.getString(key, defaultValue)
    }

    /**
     * Returns a [Long] preference for this [key].
     */
    override fun getLong(key: String, defaultValue: Long): Preference<Long> {
        return lazyStore.value.getLong(key, defaultValue)
    }

    /**
     * Returns an [Int] preference for this [key].
     */
    override fun getInt(key: String, defaultValue: Int): Preference<Int> {
        return lazyStore.value.getInt(key, defaultValue)
    }

    /**
     * Returns a [Float] preference for this [key].
     */
    override fun getFloat(key: String, defaultValue: Float): Preference<Float> {
        return lazyStore.value.getFloat(key, defaultValue)
    }

    /**
     * Returns a [Boolean] preference for this [key].
     */
    override fun getBoolean(key: String, defaultValue: Boolean): Preference<Boolean> {
        return lazyStore.value.getBoolean(key, defaultValue)
    }

    /**
     * Returns a [Set<String>] preference for this [key].
     */
    override fun getStringSet(key: String, defaultValue: Set<String>): Preference<Set<String>> {
        return lazyStore.value.getStringSet(key, defaultValue)
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
        return lazyStore.value.getObject(key, defaultValue, serializer, deserializer)
    }

    override fun <T> getJsonObject(
        key: String,
        defaultValue: T,
        serializer: KSerializer<T>,
        serializersModule: SerializersModule
    ): Preference<T> {
        return lazyStore.value.getJsonObject(key, defaultValue, serializer)
    }
}
