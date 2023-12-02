/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.reader.loader

import ca.gosyer.jui.ui.reader.model.ReaderPage
import kotlinx.coroutines.flow.StateFlow

/**
 * A loader used to load pages into the reader. Any open resources must be cleaned up when the
 * method [recycle] is called.
 */
abstract class PageLoader {
    /**
     * Whether this loader has been already recycled.
     */
    var isRecycled = false
        private set

    /**
     * Recycles this loader. Implementations must override this method to clean up any active
     * resources.
     */
    open fun recycle() {
        isRecycled = true
    }

    /**
     * Returns an [StateFlow] containing the list of pages of a chapter.
     */
    abstract fun getPages(): StateFlow<PagesState>

    /**
     * Returns an [StateFlow] that should inform of the progress of the page (see the Page class
     * for the available states)
     */
    abstract fun loadPage(page: ReaderPage)

    /**
     * Retries the given [page] in case it failed to load. This method only makes sense when an
     * online source is used.
     */
    open fun retryPage(page: ReaderPage) {}
}

sealed class PagesState {
    data object Loading : PagesState()

    data class Success(
        val pages: List<ReaderPage>,
    ) : PagesState()

    data object Empty : PagesState()
}
