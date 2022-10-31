/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.domain.chapter.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Serializable
@Immutable
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
    val fetchedAt: Long,
    val chapterCount: Int?,
    val pageCount: Int?,
    val lastReadAt: Int?,
    val downloaded: Boolean,
    val meta: ChapterMeta
)

@Serializable
@Immutable
data class ChapterMeta(
    val juiPageOffset: Int = 0
)
