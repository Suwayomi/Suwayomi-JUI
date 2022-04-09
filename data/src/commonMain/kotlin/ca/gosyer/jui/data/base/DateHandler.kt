/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.data.base

import kotlinx.datetime.Instant

expect class DateHandler {
    val formatOptions: List<String>

    fun getDateFormat(format: String): (Instant) -> String

    val dateTimeFormat: (Instant) -> String
}
