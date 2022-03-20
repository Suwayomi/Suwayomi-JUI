/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.core.lang

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@DelicateCoroutinesApi
fun launch(
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend CoroutineScope.() -> Unit
) = GlobalScope.launch(Dispatchers.Default, start, block)

@DelicateCoroutinesApi
fun launchUI(
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend CoroutineScope.() -> Unit
) = GlobalScope.launch(Dispatchers.Main, start, block)

@DelicateCoroutinesApi
fun launchIO(
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend CoroutineScope.() -> Unit
) = GlobalScope.launch(Dispatchers.IO, start, block)

fun CoroutineScope.launchDefault(
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend CoroutineScope.() -> Unit
) = launch(Dispatchers.Default, start, block)

fun CoroutineScope.launchUI(
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend CoroutineScope.() -> Unit
) = launch(Dispatchers.Main, start, block)

fun CoroutineScope.launchIO(
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend CoroutineScope.() -> Unit
) = launch(Dispatchers.IO, start, block)

suspend fun <T> withDefaultContext(
    block: suspend CoroutineScope.() -> T
): T = withContext(Dispatchers.Default, block)

suspend fun <T> withUIContext(
    block: suspend CoroutineScope.() -> T
): T = withContext(Dispatchers.Main, block)

suspend fun <T> withIOContext(
    block: suspend CoroutineScope.() -> T
): T = withContext(Dispatchers.IO, block)

fun Throwable.throwIfCancellation() { if (this is CancellationException) throw this }

fun <T> Result<T>.throwIfCancellation(): Result<T> {
    if (isFailure) {
        val exception = exceptionOrNull()
        if (exception is CancellationException) throw exception
    }
    return this
}
