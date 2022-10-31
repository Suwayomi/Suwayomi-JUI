/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.domain.source.model.sourcepreference

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Serializable
@Immutable
sealed class SourcePreference {
    abstract val props: Props<*>
}

@Immutable
interface Props<T> {
    val key: String
    val title: String?
    val summary: String?
    val currentValue: T
    val defaultValue: T
    val defaultValueType: String
}
