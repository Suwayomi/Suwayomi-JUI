/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.domain.library.model

import androidx.compose.runtime.Stable
import ca.gosyer.jui.i18n.MR
import dev.icerock.moko.resources.StringResource
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
@Stable
enum class Sort(@Transient val res: StringResource) {
    ALPHABETICAL(MR.strings.sort_alphabetical),

    // LAST_READ,
    // LAST_CHECKED,
    UNREAD(MR.strings.sort_unread),

    // TOTAL_CHAPTERS,
    // LATEST_CHAPTER,
    // DATE_FETCHED,
    DATE_ADDED(MR.strings.sort_date_added)
}
