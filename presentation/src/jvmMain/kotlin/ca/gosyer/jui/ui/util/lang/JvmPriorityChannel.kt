/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.util.lang

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import io.github.kerubistan.kroki.coroutines.priorityChannel as krokiCoroutinesPriorityChannel

actual inline fun <reified T : Comparable<T>> priorityChannel(
    maxCapacity: Int,
    scope: CoroutineScope
): Channel<T> = krokiCoroutinesPriorityChannel()
