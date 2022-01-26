/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.util.system

import ca.gosyer.core.prefs.Preference
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach

fun <T> Preference<T>.getAsFlow(action: (suspend (T) -> Unit)? = null): Flow<T> {
    val flow = merge(flowOf(get()), changes())
    return if (action != null) {
        flow.onEach(action = action)
    } else flow
}
