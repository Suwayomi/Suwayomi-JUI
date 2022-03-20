/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.util.lang

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel

@OptIn(DelicateCoroutinesApi::class)
expect inline fun <reified T : Comparable<T>> priorityChannel(
    maxCapacity: Int = 4096,
    scope: CoroutineScope = GlobalScope
): Channel<T>
