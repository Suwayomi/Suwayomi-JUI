/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.domain.download.service

import kotlinx.coroutines.flow.Flow

interface DownloadRepository {
    fun startDownloading(): Flow<Unit>

    fun stopDownloading(): Flow<Unit>

    fun clearDownloadQueue(): Flow<Unit>

    fun queueChapterDownload(chapterId: Long): Flow<Unit>

    fun stopChapterDownload(chapterId: Long): Flow<Unit>

    fun reorderChapterDownload(
        chapterId: Long,
        to: Int,
    ): Flow<Unit>

    fun batchDownload(chapterIds: List<Long>): Flow<Unit>
}
