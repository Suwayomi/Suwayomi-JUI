/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.reader.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.ImageBitmap
import ca.gosyer.jui.ui.base.model.StableHolder
import kotlinx.coroutines.flow.MutableStateFlow

@Immutable
data class ReaderPage(
    val index: Int,
    val bitmap: MutableStateFlow<StableHolder<(suspend () -> ImageDecodeState)?>>,
    val progress: MutableStateFlow<Float>,
    val status: MutableStateFlow<Status>,
    val error: MutableStateFlow<String?>,
    val chapter: ReaderChapter
) {
    enum class Status {
        QUEUE,
        WORKING,
        READY,
        ERROR
    }

    @Immutable
    sealed class ImageDecodeState {
        @Immutable
        data class Success(val bitmap: ImageBitmap) : ImageDecodeState()

        @Immutable
        object UnknownDecoder : ImageDecodeState()

        @Immutable
        object FailedToGetSnapShot : ImageDecodeState()

        @Immutable
        data class FailedToDecode(val exception: Throwable) : ImageDecodeState()
    }
}
