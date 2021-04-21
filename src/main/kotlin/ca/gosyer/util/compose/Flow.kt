/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.util.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import kotlinx.coroutines.flow.StateFlow
import kotlin.reflect.KProperty

@Composable
operator fun <T> StateFlow<T>.getValue(thisObj: Any?, property: KProperty<*>): T {
    val item by collectAsState()
    return item
}
