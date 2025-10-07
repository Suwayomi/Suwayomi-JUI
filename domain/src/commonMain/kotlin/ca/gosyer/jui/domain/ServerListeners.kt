/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.domain

import com.diamondedge.logging.logging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Inject

@Inject
class ServerListeners {
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private fun <T> Flow<T>.startWith(value: T) = onStart { emit(value) }

    private val _mangaListener = MutableSharedFlow<List<Long>>(
        extraBufferCapacity = Channel.UNLIMITED,
    )
    val mangaListener = _mangaListener.asSharedFlow()

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

    fun updateManga(ids: List<Long>) {
        val ids = ids.filter { id -> id >= 0 }
        if (ids.isEmpty()) {
            return
        }
        scope.launch {
            _mangaListener.emit(ids)
        }
    }

    fun updateManga(vararg ids: Long) {
        val ids = ids.filter { id -> id >= 0 }
        if (ids.isEmpty()) {
            return
        }
        scope.launch {
            _mangaListener.emit(ids)
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

    companion object {
        private val log = logging()
    }
}
