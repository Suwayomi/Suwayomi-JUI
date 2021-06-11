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
    var state: DownloadState = DownloadState.Queued,
    var progress: Float = 0f,
    var tries: Int = 0,
    var chapter: Chapter? = null,
)
