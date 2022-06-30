/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.domain.source.model.sourcepreference

import kotlinx.serialization.Serializable

@Serializable
data class TwoStateProps(
    override val key: String,
    override val title: String?,
    override val summary: String?,
    override val currentValue: Boolean?,
    override val defaultValue: Boolean?,
    override val defaultValueType: String
) : Props<Boolean?>
