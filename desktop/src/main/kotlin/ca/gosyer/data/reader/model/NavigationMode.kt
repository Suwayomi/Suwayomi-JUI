/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.data.reader.model

import kotlinx.serialization.Serializable

@Serializable
enum class NavigationMode(val res: String) {
    LNavigation("nav_l_shaped"),
    KindlishNavigation("nav_kindle_ish"),
    EdgeNavigation("nav_edge"),
    RightAndLeftNavigation("nav_left_right"),
}
