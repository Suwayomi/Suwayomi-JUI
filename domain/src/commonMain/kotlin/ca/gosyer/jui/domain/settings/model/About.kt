/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.domain.settings.model

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import kotlinx.serialization.Serializable

@Serializable
@Immutable
data class About(
    val name: String,
    val version: String,
    val revision: String,
    val buildType: AboutBuildType,
    val buildTime: Long,
    val github: String,
    val discord: String
)

@Serializable
@Stable
enum class AboutBuildType {
    Preview,
    Stable
}
