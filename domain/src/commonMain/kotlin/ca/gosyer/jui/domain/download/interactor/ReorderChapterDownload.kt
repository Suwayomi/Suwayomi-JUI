/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.domain.download.interactor

import ca.gosyer.jui.domain.chapter.model.Chapter
import ca.gosyer.jui.domain.download.service.DownloadRepositoryOld
import ca.gosyer.jui.domain.manga.model.Manga
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import me.tatarka.inject.annotations.Inject
import org.lighthousegames.logging.logging

class ReorderChapterDownload
    @Inject
    constructor(
        private val downloadRepositoryOld: DownloadRepositoryOld,
    ) {
        suspend fun await(
            mangaId: Long,
            index: Int,
            to: Int,
            onError: suspend (Throwable) -> Unit = {},
        ) = asFlow(mangaId, index, to)
            .catch {
                onError(it)
                log.warn(it) { "Failed to reorder chapter download for $index of $mangaId to $to" }
            }
            .collect()

        suspend fun await(
            manga: Manga,
            index: Int,
            to: Int,
            onError: suspend (Throwable) -> Unit = {},
        ) = asFlow(manga, index, to)
            .catch {
                onError(it)
                log.warn(it) { "Failed to reorder chapter download for $index of ${manga.title}(${manga.id}) to $to" }
            }
            .collect()

        suspend fun await(
            chapter: Chapter,
            to: Int,
            onError: suspend (Throwable) -> Unit = {},
        ) = asFlow(chapter, to)
            .catch {
                onError(it)
                log.warn(it) { "Failed to reorder chapter download for ${chapter.index} of ${chapter.mangaId} to $to" }
            }
            .collect()

        fun asFlow(
            mangaId: Long,
            index: Int,
            to: Int,
        ) = downloadRepositoryOld.reorderChapterDownload(mangaId, index, to)

        fun asFlow(
            manga: Manga,
            index: Int,
            to: Int,
        ) = downloadRepositoryOld.reorderChapterDownload(manga.id, index, to)

        fun asFlow(
            chapter: Chapter,
            to: Int,
        ) = downloadRepositoryOld.reorderChapterDownload(chapter.mangaId, chapter.index, to)

        companion object {
            private val log = logging()
        }
    }
