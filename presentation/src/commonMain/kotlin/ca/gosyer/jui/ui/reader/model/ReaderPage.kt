/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.reader.model

import androidx.compose.ui.graphics.ImageBitmap
import kotlinx.coroutines.flow.MutableStateFlow

data class ReaderPage(
    val index: Int,
    val bitmap: MutableStateFlow<ImageBitmap?>,
    val progress: MutableStateFlow<Float>,
    val status: MutableStateFlow<Status>,
    val error: MutableStateFlow<String?>,
    val chapter: ReaderChapter
) {
    enum class Status {
        QUEUE,
        READY,
        ERROR
    }
}
