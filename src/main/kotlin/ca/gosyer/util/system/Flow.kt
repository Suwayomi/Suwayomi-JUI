/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.util.system

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch

fun <T> Flow<T>.asStateFlow(defaultValue: T, scope: CoroutineScope, dropFirst: Boolean = false): StateFlow<T> {
    val flow = MutableStateFlow(defaultValue)
    scope.launch {
        if (dropFirst) {
            drop(1)
        } else {
            this@asStateFlow
        }.collect {
            flow.value = it
        }
    }
    return flow.asStateFlow()
}