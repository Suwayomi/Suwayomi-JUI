/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.data.models.sourcepreference

import kotlinx.serialization.Serializable

@Serializable
sealed class SourcePreference {
    abstract val props: Props<*>
}

interface Props<T> {
    val key: String
    val title: String?
    val summary: String?
    val currentValue: T
    val defaultValue: T
    val defaultValueType: String
}
