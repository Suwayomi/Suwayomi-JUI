/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.domain.backup.model

import kotlinx.serialization.Serializable

enum class RestoreState {
    IDLE,
    SUCCESS,
    FAILURE,
    RESTORING_CATEGORIES,
    RESTORING_MANGA,
    RESTORING_META,
    RESTORING_SETTINGS,
    UNKNOWN,
}

@Serializable
data class RestoreStatus(
    val state: RestoreState,
    val completed: Int,
    val total: Int,
)
