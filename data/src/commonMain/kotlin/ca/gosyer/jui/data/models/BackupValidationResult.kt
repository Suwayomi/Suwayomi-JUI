/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.data.models

import kotlinx.serialization.Serializable

@Serializable
data class BackupValidationResult(
    val missingSources: List<String>,
    val missingTrackers: List<String>
)
