/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.domain.download.model

import ca.gosyer.jui.domain.chapter.model.Chapter
import ca.gosyer.jui.domain.manga.model.Manga
import kotlinx.serialization.Serializable

@Serializable
data class DownloadChapter(
    val chapterIndex: Int,
    val mangaId: Long,
    val chapter: Chapter,
    val manga: Manga,
    val state: DownloadState = DownloadState.Queued,
    val progress: Float = 0f,
    val tries: Int = 0
)
