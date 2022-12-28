/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.reader.model

import kotlinx.coroutines.flow.MutableStateFlow

data class ViewerChapters(
    val currChapter: MutableStateFlow<ReaderChapter?>,
    val prevChapter: MutableStateFlow<ReaderChapter?>,
    val nextChapter: MutableStateFlow<ReaderChapter?>
) {
    fun recycle() {
        currChapter.value?.recycle()
        prevChapter.value?.recycle()
        nextChapter.value?.recycle()
        currChapter.value = null
        prevChapter.value = null
        nextChapter.value = null
    }

    fun movePrev() {
        nextChapter.value?.recycle()
        nextChapter.value = currChapter.value
        currChapter.value = prevChapter.value
        prevChapter.value = null
    }

    fun moveNext() {
        prevChapter.value?.recycle()
        prevChapter.value = currChapter.value
        currChapter.value = nextChapter.value
        nextChapter.value = null
    }
}
