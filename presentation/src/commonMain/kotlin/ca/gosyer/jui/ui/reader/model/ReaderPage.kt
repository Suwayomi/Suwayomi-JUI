/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.reader.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.IntSize
import ca.gosyer.jui.ui.base.model.StableHolder
import kotlinx.coroutines.flow.MutableStateFlow

@Immutable
data class ReaderPage(
    val url: String,
    val index2: Int,
    val bitmap: MutableStateFlow<StableHolder<(suspend () -> ImageDecodeState)?>>,
    val bitmapInfo: MutableStateFlow<BitmapInfo?>,
    val progress: MutableStateFlow<Float>,
    val status: MutableStateFlow<Status>,
    val error: MutableStateFlow<String?>,
    val chapter: ReaderChapter,
) : ReaderItem() {
    enum class Status {
        QUEUE,
        WORKING,
        READY,
        ERROR,
    }

    @Immutable
    data class BitmapInfo(
        val size: IntSize,
    )

    @Immutable
    sealed class ImageDecodeState {
        @Immutable
        data class Success(
            val bitmap: ImageBitmap,
        ) : ImageDecodeState()

        @Immutable
        data object UnknownDecoder : ImageDecodeState()

        @Immutable
        data object FailedToGetSnapShot : ImageDecodeState()

        @Immutable
        data class FailedToDecode(
            val exception: Throwable,
        ) : ImageDecodeState()
    }
}
