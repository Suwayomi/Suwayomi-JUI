/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.reader.model

import androidx.compose.runtime.Immutable
import ca.gosyer.jui.domain.chapter.model.Chapter
import ca.gosyer.jui.ui.reader.loader.PageLoader
import ca.gosyer.jui.ui.reader.loader.PagesState
import com.diamondedge.logging.logging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

@Immutable
data class ReaderChapter(
    val chapter: Chapter,
) {
    val scope = CoroutineScope(Dispatchers.Default + Job())

    private val _state = MutableStateFlow<State>(State.Wait)

    var state: State
        get() = _state.value
        set(value) {
            _state.value = value
        }

    val stateObserver = _state.asStateFlow()

    val pages: StateFlow<PagesState> = _state.filterIsInstance<State.Loaded>()
        .flatMapLatest { it.pages }
        .stateIn(scope, SharingStarted.Eagerly, PagesState.Loading)

    var pageLoader: PageLoader? = null

    var requestedPage: Int = 0

    fun recycle() {
        if (pageLoader != null) {
            log.debug { "Recycling chapter ${chapter.name}" }
        }
        pageLoader?.recycle()
        pageLoader = null
        state = State.Wait
        scope.cancel()
    }

    sealed class State {
        data object Wait : State()

        data object Loading : State()

        class Error(
            val error: Throwable,
        ) : State()

        class Loaded(
            val pages: StateFlow<PagesState>,
        ) : State()
    }

    private companion object {
        private val log = logging()
    }
}
