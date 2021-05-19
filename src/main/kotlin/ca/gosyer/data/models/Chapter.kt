/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.data.models

import kotlinx.serialization.Serializable

@Serializable
data class Chapter(
    val url: String,
    val name: String,
    val uploadDate: Long,
    val chapterNumber: Float,
    val scanlator: String?,
    val mangaId: Long,
    val read: Boolean,
    val bookmarked: Boolean,
    val lastPageRead: Int,
    val index: Int,
    val chapterCount: Int?,
    val pageCount: Int?,
)
