/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.util.compose

import com.github.zsoltk.compose.savedinstancestate.Bundle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

inline fun <reified T> Bundle.putJsonObject(key: String, item: T) {
    putString(key, Json.encodeToString(item))
}

inline fun <reified T> Bundle.getJsonObject(key: String): T? {
    return getString(key)?.let { Json.decodeFromString(it) }
}

inline fun <reified T> Bundle.getJsonObjectArray(key: String): List<T?>? {
    return getString(key)?.let { Json.decodeFromString(it) }
}

inline fun <T> saveAnyInBundle(
    scope: CoroutineScope,
    bundle: Bundle,
    key: String,
    getValue: Bundle.(String) -> T?,
    crossinline putValue: Bundle.(itemKey: String, item: T) -> Unit,
    initialValue: () -> T
): MutableStateFlow<T> {
    val item = bundle.getValue(key)
    val flow: MutableStateFlow<T> = if (item != null) {
        MutableStateFlow(item)
    } else {
        MutableStateFlow(initialValue())
    }
    flow.drop(1)
        .onEach { bundle.putValue(key, it) }
        .launchIn(scope)

    return flow
}

inline fun <reified T> saveObjectInBundle(
    scope: CoroutineScope,
    bundle: Bundle,
    key: String,
    initialValue: () -> T
): MutableStateFlow<T> {
    return saveAnyInBundle(
        scope,
        bundle,
        key,
        { getJsonObject<T>(it) },
        { itemKey, item ->
            putJsonObject(itemKey, item)
        },
        initialValue
    )
}

fun saveIntInBundle(
    scope: CoroutineScope,
    bundle: Bundle,
    key: String,
    initialValue: Int
): MutableStateFlow<Int> {
    return saveAnyInBundle(
        scope,
        bundle,
        key,
        { getInt(key, initialValue) },
        { itemKey, item ->
            putInt(itemKey, item)
        },
        { initialValue }
    )
}

fun saveBooleanInBundle(
    scope: CoroutineScope,
    bundle: Bundle,
    key: String,
    initialValue: Boolean
): MutableStateFlow<Boolean> {
    return saveAnyInBundle(
        scope,
        bundle,
        key,
        { getBoolean(key, initialValue) },
        { itemKey, item ->
            putBoolean(itemKey, item)
        },
        { initialValue }
    )
}

fun saveStringInBundle(
    scope: CoroutineScope,
    bundle: Bundle,
    key: String,
    initialValue: () -> String? = { null }
): MutableStateFlow<String?> {
    return saveAnyInBundle(
        scope,
        bundle,
        key,
        { getString(key) ?: initialValue() },
        { itemKey, item ->
            if (item != null) {
                putString(itemKey, item)
            } else remove(itemKey)
        },
        initialValue
    )
}
