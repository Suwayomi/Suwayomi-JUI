/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.data.reader.model

import kotlinx.serialization.Serializable

@Serializable
enum class ImageScale(val res: String) {
    FitScreen("scale_fit_screen"),
    Stretch("scale_stretch"),
    FitWidth("scale_fit_width"),
    FitHeight("scale_fit_height"),
    OriginalSize("scale_original"),
    SmartFit("scale_smart"),
}
