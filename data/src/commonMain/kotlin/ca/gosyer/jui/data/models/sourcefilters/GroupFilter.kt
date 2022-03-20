/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.data.models.sourcefilters

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("Group")
data class GroupFilter(
    override val filter: GroupProps
) : SourceFilter() {
    @Serializable
    data class GroupProps(
        override val name: String,
        override val state: List<SourceFilter>
    ) : Props<List<SourceFilter>>
}
