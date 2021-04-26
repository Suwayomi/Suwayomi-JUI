/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.util.compose

import com.github.zsoltk.compose.savedinstancestate.Bundle
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

inline fun <reified T> Bundle.putJsonObject(key: String, item: T) {
    putString(key, Json.encodeToString(item))
}

inline fun <reified T> Bundle.getJsonObject(key: String): T? {
    return getString(key)?.let { Json.decodeFromString(it) }
}

inline fun <reified T> Bundle.putJsonObjectArray(key: String, items: T) {
    putString(key, Json.encodeToString(items))
}

inline fun <reified T> Bundle.getJsonObjectArray(key: String): List<T?>? {
    return getString(key)?.let { Json.decodeFromString(it) }
}
