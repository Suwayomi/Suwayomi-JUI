/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.data.reader.model

enum class DefaultReaderMode(
    val res: String,
    val continuous: Boolean,
    val direction: Direction = Direction.Down,
    val padding: Int = 0,
    val imageScale: ImageScale = ImageScale.FitScreen,
    val navigationMode: NavigationMode = NavigationMode.LNavigation,
) {
    RTL("RTL", false, Direction.Left, navigationMode = NavigationMode.RightAndLeftNavigation),
    LTR("LTR", false, Direction.Right, navigationMode = NavigationMode.RightAndLeftNavigation),
    Vertical("Vertical", false),
    ContinuesVertical("Continues Vertical", true, padding = 16),
    LongStrip("Long Strip", true)
}
