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
            rect = Rect(0, 0, 66, 100),
            type = Navigation.LEFT
        ),
        Region(
            rect = Rect(66, 0, 100, 100),
            type = Navigation.RIGHT
        )
    )
}
