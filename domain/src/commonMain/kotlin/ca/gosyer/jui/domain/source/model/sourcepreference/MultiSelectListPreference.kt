/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.domain.source.model.sourcepreference

import androidx.compose.runtime.Immutable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("MultiSelectListPreference")
@Immutable
data class MultiSelectListPreference(override val props: MultiSelectListProps) : SourcePreference() {
    @Serializable
    @Immutable
    data class MultiSelectListProps(
        override val key: String,
        override val title: String,
        override val summary: String?,
        override val currentValue: List<String>?,
        override val defaultValue: List<String>?,
        override val defaultValueType: String,
        val dialogTitle: String?,
        val dialogMessage: String?,
        val entries: List<String>,
        val entryValues: List<String>,
    ) : Props<List<String>?>
}
