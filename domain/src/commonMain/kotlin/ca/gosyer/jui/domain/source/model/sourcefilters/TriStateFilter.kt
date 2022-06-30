/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.domain.source.model.sourcefilters

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("TriState")
data class TriStateFilter(
    override val filter: TriStateProps
) : SourceFilter() {
    @Serializable
    data class TriStateProps(
        override val name: String,
        override val state: Int
    ) : Props<Int>
}
