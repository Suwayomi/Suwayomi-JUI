/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.domain.source.model.sourcefilters

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
@SerialName("Select")
data class SelectFilterOld(
    override val filter: SelectProps,
) : SourceFilterOld() {
    @Serializable
    data class SelectProps(
        override val name: String,
        override val state: Int,
        val values: List<JsonElement>,
        val displayValues: List<String>? = null,
    ) : Props<Int>
}
