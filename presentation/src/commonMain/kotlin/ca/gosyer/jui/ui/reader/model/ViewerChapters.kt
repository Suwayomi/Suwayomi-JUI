/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.reader.model

data class ViewerChapters(
    val currChapter: ReaderChapter?,
    val prevChapter: ReaderChapter?,
    val nextChapter: ReaderChapter?,
) {
    fun recycle() {
        currChapter?.recycle()
        prevChapter?.recycle()
        nextChapter?.recycle()
    }

    fun movePrev(): ViewerChapters {
        nextChapter?.recycle()
        return ViewerChapters(
            nextChapter = currChapter,
            currChapter = prevChapter,
            prevChapter = null,
        )
    }

    fun moveNext(): ViewerChapters {
        prevChapter?.recycle()
        return ViewerChapters(
            prevChapter = currChapter,
            currChapter = nextChapter,
            nextChapter = null,
        )
    }
}
