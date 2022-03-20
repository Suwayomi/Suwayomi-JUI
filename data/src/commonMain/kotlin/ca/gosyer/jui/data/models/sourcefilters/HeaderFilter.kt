/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.data.models.sourcefilters

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
@SerialName("Header")
data class HeaderFilter(
    override val filter: HeaderProps
) : SourceFilter() {
    @Serializable
    data class HeaderProps(
        override val name: String,
        @Transient
        override val state: Int = 0
    ) : Props<Int>
}
