/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.data.models.sourcefilters

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("Sort")
data class SortFilter(
    override val filter: SortProps
) : SourceFilter() {
    @Serializable
    data class SortProps(
        override val name: String,
        override val state: Selection?,
        val values: List<String>
    ) : Props<Selection?>

    @Serializable
    data class Selection(
        val index: Int,
        val ascending: Boolean
    )
}
