/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.data.reader.model

import kotlinx.serialization.Serializable

@Serializable
enum class NavigationMode(val res: String) {
    LNavigation("L shaped"),
    KindlishNavigation("Kindle-ish"),
    EdgeNavigation("Edge"),
    RightAndLeftNavigation("Right and Left"),
}
