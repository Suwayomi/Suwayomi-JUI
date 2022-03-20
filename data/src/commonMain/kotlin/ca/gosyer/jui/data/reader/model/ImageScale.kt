/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.data.reader.model

import ca.gosyer.jui.i18n.MR
import dev.icerock.moko.resources.StringResource
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
enum class ImageScale(@Transient val res: StringResource) {
    FitScreen(MR.strings.scale_fit_screen),
    Stretch(MR.strings.scale_stretch),
    FitWidth(MR.strings.scale_fit_width),
    FitHeight(MR.strings.scale_fit_height),
    OriginalSize(MR.strings.scale_original),
    SmartFit(MR.strings.scale_smart),
}
