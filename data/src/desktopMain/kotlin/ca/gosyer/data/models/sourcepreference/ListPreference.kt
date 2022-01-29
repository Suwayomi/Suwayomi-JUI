/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.data.models.sourcepreference

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("ListPreference")
data class ListPreference(override val props: ListProps) : SourcePreference() {
    @Serializable
    data class ListProps(
        override val key: String,
        override val title: String,
        override val summary: String?,
        override val currentValue: String?,
        override val defaultValue: String?,
        override val defaultValueType: String,
        val entries: List<String>,
        val entryValues: List<String>
    ) : Props<String?>
}
