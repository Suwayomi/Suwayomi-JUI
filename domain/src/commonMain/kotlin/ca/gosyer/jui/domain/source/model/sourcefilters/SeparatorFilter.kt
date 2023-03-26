/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.domain.source.model.sourcefilters

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
@SerialName("Separator")
data class SeparatorFilter(
    override val filter: SeparatorProps,
) : SourceFilter() {
    @Serializable
    data class SeparatorProps(
        override val name: String,
        @Transient
        override val state: Int = 0,
    ) : Props<Int>
}
