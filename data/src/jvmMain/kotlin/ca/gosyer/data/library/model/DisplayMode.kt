/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.data.library.model

import ca.gosyer.i18n.MR
import dev.icerock.moko.resources.StringResource
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
enum class DisplayMode(@Transient val res: StringResource) {
    CompactGrid(MR.strings.display_compact),
    ComfortableGrid(MR.strings.display_comfortable),
    CoverOnlyGrid(MR.strings.display_cover_only),
    List(MR.strings.display_list);

    companion object {
        val values = values()
    }
}
