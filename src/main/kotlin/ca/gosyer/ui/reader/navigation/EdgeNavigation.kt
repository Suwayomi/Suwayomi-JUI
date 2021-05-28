package ca.gosyer.ui.reader.navigation

import ca.gosyer.ui.reader.model.Navigation

/**
 * Visualization of default state without any inversion
 * +---+---+---+
 * | N | N | N |   P: Previous
 * +---+---+---+
 * | N | M | N |   M: Menu
 * +---+---+---+
 * | N | P | N |   N: Next
 * +---+---+---+
*/
class EdgeNavigation : ViewerNavigation() {

    override var regions: List<Region> = listOf(
        Region(
            rect = Rect(0, 0, 33, 100),
            type = Navigation.NEXT
        ),
        Region(
            rect = Rect(33, 66, 66, 100),
            type = Navigation.PREV
        ),
        Region(
            rect = Rect(66, 0, 100, 100),
            type = Navigation.NEXT
        ),
    )
}
