/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.backend.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Chapter(
    val id: Long,
    val url: String,
    val name: String,
    @SerialName("date_upload")
    val dateUpload: Long,
    @SerialName("chapter_number")
    val chapterNumber: Float,
    val scanlator: String?,
    val mangaId: Long,
    val pageCount: Int? = null,
)
