/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.domain

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Inject
import org.lighthousegames.logging.logging

class ServerListeners @Inject constructor() {
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private fun <T> Flow<T>.startWith(value: T) = onStart { emit(value) }

    private val mangaListener = MutableSharedFlow<List<Long>>(
        extraBufferCapacity = Channel.UNLIMITED,
    )

    private val chapterIndexesListener = MutableSharedFlow<Pair<Long, List<Int>?>>(
        extraBufferCapacity = Channel.UNLIMITED,
    )

    private val chapterIdsListener = MutableSharedFlow<Pair<Long?, List<Long>>>(
        extraBufferCapacity = Channel.UNLIMITED,
    )

    private val categoryMangaListener = MutableSharedFlow<Long>(
        extraBufferCapacity = Channel.UNLIMITED,
    )

    private val extensionListener = MutableSharedFlow<List<String>>(
        extraBufferCapacity = Channel.UNLIMITED,
    )

    fun <T> combineMangaUpdates(flow: Flow<T>, predate: (suspend (List<Long>) -> Boolean)? = null) =
        if (predate != null) {
            mangaListener
                .filter(predate)
                .startWith(Unit)
        } else {
            mangaListener.startWith(Unit)
        }
            .buffer(capacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
            .flatMapLatest { flow }

    fun updateManga(vararg ids: Long) {
        scope.launch {
            mangaListener.emit(ids.toList())
        }
    }

    fun <T> combineCategoryManga(flow: Flow<T>, predate: (suspend (Long) -> Boolean)? = null) =
        if (predate != null) {
            categoryMangaListener.filter(predate).startWith(-1)
        } else {
            categoryMangaListener.startWith(-1)
        }
            .buffer(capacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
            .flatMapLatest { flow }

    fun updateCategoryManga(id: Long) {
        scope.launch {
            categoryMangaListener.emit(id)
        }
    }

    fun <T> combineChapters(
        flow: Flow<T>,
        indexPredate: (suspend (Long, List<Int>?) -> Boolean)? = null,
        idPredate: (suspend (Long?, List<Long>) -> Boolean)? = null
    ): Flow<T> {
        val indexListener = if (indexPredate != null) {
            chapterIndexesListener.filter { indexPredate(it.first, it.second) }.startWith(Unit)
        } else {
            chapterIndexesListener.startWith(Unit)
        }
        val idsListener = if (idPredate != null) {
            chapterIdsListener.filter { idPredate(it.first, it.second) }.startWith(Unit)
        } else {
            chapterIdsListener.startWith(Unit)
        }

        return combine(indexListener, idsListener) { _, _ -> }
            .buffer(capacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
            .flatMapLatest { flow }
    }

    fun updateChapters(mangaId: Long, chapterIndexes: List<Int>) {
        scope.launch {
            chapterIndexesListener.emit(mangaId to chapterIndexes.ifEmpty { null })
        }
    }

    fun updateChapters(mangaId: Long, vararg chapterIndexes: Int) {
        scope.launch {
            chapterIndexesListener.emit(mangaId to chapterIndexes.toList().ifEmpty { null })
        }
    }

    fun updateChapters(mangaId: Long?, chapterIds: List<Long>) {
        scope.launch {
            chapterIdsListener.emit(mangaId to chapterIds)
        }
    }

    fun updateChapters(mangaId: Long?, vararg chapterIds: Long) {
        scope.launch {
            chapterIdsListener.emit(mangaId to chapterIds.toList())
        }
    }

    companion object {
        private val log = logging()
    }
}