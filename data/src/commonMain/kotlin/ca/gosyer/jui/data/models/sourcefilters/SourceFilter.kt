/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.data.models.sourcefilters

import kotlinx.serialization.Serializable

@Serializable
sealed class SourceFilter {
    abstract val filter: Props<*>
}

interface Props<T> {
    val name: String
    val state: T
}
