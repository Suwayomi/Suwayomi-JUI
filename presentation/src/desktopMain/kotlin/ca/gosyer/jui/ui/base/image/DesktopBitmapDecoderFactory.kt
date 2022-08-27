/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.base.image

import ca.gosyer.jui.uicore.vm.ContextWrapper
import com.seiko.imageloader.component.decoder.Decoder
import com.seiko.imageloader.component.decoder.SkiaImageDecoder

actual class BitmapDecoderFactory actual constructor(contextWrapper: ContextWrapper) :
    Decoder.Factory by SkiaImageDecoder.Factory()
