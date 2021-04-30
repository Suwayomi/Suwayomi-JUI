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
    val padding: Float = 0F,
    val imageScale: ImageScale = ImageScale.FitScreen
) {
    RTL("RTL", false, Direction.Left),
    LTR("LTR", false, Direction.Right),
    Vertical("Vertical", false),
    ContinuesVertical("Continues Vertical", true, padding = 1.0F),
    LongStrip("Long Strip", true)
}
