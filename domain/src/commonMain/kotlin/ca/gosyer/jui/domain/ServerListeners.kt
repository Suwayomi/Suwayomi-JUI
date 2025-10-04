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
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Inject
import org.lighthousegames.logging.logging

@Inject
class ServerListeners {
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private fun <T> Flow<T>.startWith(value: T) = onStart { emit(value) }

    private val _mangaListener = MutableSharedFlow<List<Long>>(
        extraBufferCapacity = Channel.UNLIMITED,
    )
    val mangaListener = _mangaListener.asSharedFlow()

    private val _chapterIdsListener = MutableSharedFlow<List<Long>>(
        extraBufferCapacity = Channel.UNLIMITED,
    )
    val chapterIdsListener = _chapterIdsListener.asSharedFlow()

    private val _mangaChapterIdsListener = MutableSharedFlow<List<Long>>(
        extraBufferCapacity = Channel.UNLIMITED,
    )
    val mangaChapterIdsListener = _mangaChapterIdsListener.asSharedFlow()

    private val categoryMangaListener = MutableSharedFlow<Long>(
        extraBufferCapacity = Channel.UNLIMITED,
    )

    private val extensionListener = MutableSharedFlow<List<String>>(
        extraBufferCapacity = Channel.UNLIMITED,
    )

    fun <T> combineMangaUpdates(
        flow: Flow<T>,
        predate: (suspend (List<Long>) -> Boolean)? = null,
    ) = if (predate != null) {
        _mangaListener
            .filter(predate)
            .startWith(Unit)
    } else {
        _mangaListener.startWith(Unit)
    }
        .buffer(capacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
        .flatMapLatest { flow }

    fun updateManga(vararg ids: Long) {
        scope.launch {
            _mangaListener.emit(ids.toList())
        }
    }

    fun <T> combineCategoryManga(
        flow: Flow<T>,
        predate: (suspend (Long) -> Boolean)? = null,
    ) = if (predate != null) {
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
        chapterIdPredate: (suspend (List<Long>) -> Boolean)? = null,
        mangaIdPredate: (suspend (List<Long>) -> Boolean)? = null,
    ): Flow<T> {
        val idsListener = _chapterIdsListener
            .filter { chapterIdPredate?.invoke(it) ?: false }
            .startWith(Unit)
            .combine(
                _mangaChapterIdsListener.filter { mangaIdPredate?.invoke(it) ?: false }
                    .startWith(Unit),
            ) { _, _ -> }

        return idsListener
            .buffer(capacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
            .flatMapLatest { flow }
    }

    fun updateChapters(chapterIds: List<Long>) {
        scope.launch {
            _chapterIdsListener.emit(chapterIds)
        }
    }

    fun updateChapters(vararg chapterIds: Long) {
        scope.launch {
            _chapterIdsListener.emit(chapterIds.toList())
        }
    }

    companion object {
        private val log = logging()
    }
}
