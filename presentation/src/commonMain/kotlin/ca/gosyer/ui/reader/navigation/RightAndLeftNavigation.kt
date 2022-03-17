/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.reader.navigation

import ca.gosyer.ui.reader.model.Navigation

/**
 * Visualization of default state without any inversion
 * +---+---+---+
 * | N | N | P |   P: Move Right
 * +---+---+---+
 * | N | N | P |   M: Menu
 * +---+---+---+
 * | N | N | P |   N: Move Left
 * +---+---+---+
 */
class RightAndLeftNavigation : ViewerNavigation() {

    override var regions: List<Region> = listOf(
        Region(
            rect = Rect(0, 0, 33, 100),
            type = Navigation.LEFT
        ),
        Region(
            rect = Rect(66, 0, 100, 100),
            type = Navigation.RIGHT
        )
    )
}
