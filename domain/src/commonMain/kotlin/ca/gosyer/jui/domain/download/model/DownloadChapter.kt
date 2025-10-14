/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.domain.download.model

data class DownloadQueueItem(
    val position: Int,
    val progress: Float,
    val state: DownloadState,
    val tries: Int,
    val chapter: DownloadChapter,
    val manga: DownloadManga
)

data class DownloadChapter(
    val id: Long,
    val name: String,
    val pageCount: Int,
)

data class DownloadManga(
    val id: Long,
    val title: String,
    val thumbnailUrl: String?,
    val thumbnailUrlLastFetched: Long = 0,
)
