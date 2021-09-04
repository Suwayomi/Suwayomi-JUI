/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.data.models

import kotlinx.serialization.Serializable

@Serializable
data class Manga(
    val id: Long,
    val sourceId: Long,
    val url: String,
    val title: String,
    val thumbnailUrl: String?,
    val initialized: Boolean,
    val artist: String?,
    val author: String?,
    val description: String?,
    val genre: List<String>,
    val status: String,
    val inLibrary: Boolean,
    val source: Source?,
    val freshData: Boolean,
    val meta: MangaMeta,
    val realUrl: String?
) {
    fun cover(serverUrl: String) = thumbnailUrl?.let { serverUrl + it }
}

@Serializable
data class MangaMeta(
    val jui: Int? = null
)
