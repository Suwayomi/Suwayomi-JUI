/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.reader.navigation

import androidx.compose.runtime.Immutable
import ca.gosyer.jui.ui.reader.model.Navigation

/**
 * Visualization of default state without any inversion
 * +---+---+---+
 * | M | M | M |   P: Previous
 * +---+---+---+
 * | P | N | N |   M: Menu
 * +---+---+---+
 * | P | N | N |   N: Next
 * +---+---+---+
*/
@Immutable
class KindlishNavigation : ViewerNavigation() {
    override var regions: List<Region> = listOf(
        Region(
            rect = Rect(33, 33, 100, 100),
            type = Navigation.NEXT,
        ),
        Region(
            rect = Rect(0, 33, 33, 100),
            type = Navigation.PREV,
        ),
    )
}
