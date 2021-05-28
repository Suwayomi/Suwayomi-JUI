package ca.gosyer.ui.reader.navigation

import ca.gosyer.ui.reader.model.Navigation

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
class KindlishNavigation : ViewerNavigation() {

    override var regions: List<Region> = listOf(
        Region(
            rect = Rect(33, 33, 100, 100),
            type = Navigation.NEXT
        ),
        Region(
            rect = Rect(0, 33, 33, 100),
            type = Navigation.PREV
        )
    )
}
