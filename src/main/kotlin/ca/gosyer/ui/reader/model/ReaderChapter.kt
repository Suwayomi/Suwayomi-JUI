/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.reader.model

import ca.gosyer.data.models.Chapter
import ca.gosyer.ui.reader.loader.PageLoader
import ca.gosyer.util.system.CKLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.coroutines.CoroutineContext

data class ReaderChapter(val context: CoroutineContext, val chapter: Chapter) {
    var scope = CoroutineScope(context + Job())
        private set

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
