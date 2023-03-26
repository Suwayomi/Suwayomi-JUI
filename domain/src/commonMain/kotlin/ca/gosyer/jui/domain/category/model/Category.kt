/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.domain.category.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Serializable
@Immutable
data class Category(
    val id: Long,
    val order: Int,
    val name: String,
    val default: Boolean,
    val meta: CategoryMeta,
)

@Serializable
@Immutable
data class CategoryMeta(
    val example: Int = 0,
)
