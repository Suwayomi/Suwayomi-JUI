/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.data.library.model

import kotlinx.serialization.Serializable

@Serializable
enum class Sort {
    ALPHABETICAL,

    // LAST_READ,
    // LAST_CHECKED,
    UNREAD,

    // TOTAL_CHAPTERS,
    // LATEST_CHAPTER,
    // DATE_FETCHED,
    DATE_ADDED;
}
