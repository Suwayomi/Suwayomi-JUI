/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.data.download.model

import ca.gosyer.data.models.Chapter
import kotlinx.serialization.Serializable

@Serializable
data class DownloadChapter(
    val chapterIndex: Int,
    val mangaId: Long,
    val state: DownloadState = DownloadState.Queued,
    val progress: Float = 0f,
    val tries: Int = 0,
    val chapter: Chapter? = null,
)
