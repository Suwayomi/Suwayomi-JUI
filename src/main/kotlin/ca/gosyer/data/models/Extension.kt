/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.data.models

import kotlinx.serialization.Serializable

@Serializable
data class Extension(
    val name: String,
    val pkgName: String,
    val versionName: String,
    val versionCode: Int,
    val lang: String,
    val apkName: String,
    val iconUrl: String,
    val installed: Boolean,
    val classFQName: String,
    val nsfw: Boolean
) {
    fun iconUrl(serverUrl: String) = serverUrl + iconUrl
}
