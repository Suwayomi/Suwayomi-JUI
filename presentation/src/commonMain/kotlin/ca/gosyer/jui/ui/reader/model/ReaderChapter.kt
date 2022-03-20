/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.reader.model

import ca.gosyer.jui.core.logging.CKLogger
import ca.gosyer.jui.data.models.Chapter
import ca.gosyer.jui.ui.reader.loader.PageLoader
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ReaderChapter(val chapter: Chapter) {
    val scope = CoroutineScope(Dispatchers.Default + Job())

    var state: State =
        State.Wait
        set(value) {
                field = value
                stateRelay.value = value
            }

    private val stateRelay by lazy { MutableStateFlow(state) }

    val stateObserver by lazy { stateRelay.asStateFlow() }

    val pages: StateFlow<List<ReaderPage>>?
        get() = (state as? State.Loaded)?.pages

    var pageLoader: PageLoader? = null

    var requestedPage: Int = 0

    fun recycle() {
        if (pageLoader != null) {
            debug { "Recycling chapter ${chapter.name}" }
        }
        pageLoader?.recycle()
        pageLoader = null
        state = State.Wait
        scope.cancel()
    }

    sealed class State {
        object Wait : State()
        object Loading : State()
        class Error(val error: Throwable) : State()
        class Loaded(val pages: StateFlow<List<ReaderPage>>) : State()
    }

    private companion object : CKLogger({})
}
