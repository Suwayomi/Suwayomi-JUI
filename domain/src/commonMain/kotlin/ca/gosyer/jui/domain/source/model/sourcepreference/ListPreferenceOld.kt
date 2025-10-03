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
@SerialName("ListPreference")
@Immutable
data class ListPreferenceOld(
    override val props: ListProps,
) : SourcePreferenceOld() {
    @Serializable
    @Immutable
    data class ListProps(
        override val key: String,
        override val title: String,
        override val summary: String?,
        override val currentValue: String?,
        override val defaultValue: String?,
        override val defaultValueType: String,
        val entries: List<String>,
        val entryValues: List<String>,
    ) : Props<String?>
}
