/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.domain.source.model.sourcefilters

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class SourceFilterChange(val position: Int, val state: String) {
    constructor(position: Int, state: Any) : this(
        position,
        if (state is SortFilter.Selection) {
            Json.encodeToString(state)
        } else {
            state.toString()
        }
    )
}
