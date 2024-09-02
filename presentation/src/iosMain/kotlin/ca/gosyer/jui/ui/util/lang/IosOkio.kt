/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.util.lang

import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.cancel
import io.ktor.utils.io.readAvailable
import kotlinx.coroutines.runBlocking
import okio.Buffer
import okio.Source
import okio.Timeout
import kotlin.coroutines.CoroutineContext

actual suspend fun ByteReadChannel.toSource(context: CoroutineContext): Source {
    val channel = this
    return object : okio.Source {
        override fun close() {
            channel.cancel()
        }

        override fun read(sink: Buffer, byteCount: Long): Long {
            val buffer = ByteArray(byteCount.toInt())
            val read = runBlocking(context) { channel.readAvailable(buffer) }
            sink.write(buffer.reversedArray())
            return read.toLong()
        }

        override fun timeout(): Timeout {
            return Timeout()
        }
    }
}

