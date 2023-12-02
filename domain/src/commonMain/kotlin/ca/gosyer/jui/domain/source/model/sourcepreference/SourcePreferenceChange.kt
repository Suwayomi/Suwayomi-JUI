/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.domain.source.model.sourcepreference

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class SourcePreferenceChange(
    val position: Int,
    val value: String,
) {
    constructor(position: Int, value: Any) : this(
        position,
        if (value is List<*>) {
            @Suppress("UNCHECKED_CAST")
            Json.encodeToString(value as List<String>)
        } else {
            value.toString()
        },
    )
}
