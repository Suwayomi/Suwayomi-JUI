/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.core.io

import ca.gosyer.jui.core.lang.withIOContext
import io.ktor.utils.io.ByteReadChannel
import okio.Buffer
import okio.BufferedSink
import okio.BufferedSource
import okio.FileSystem
import okio.Path
import okio.Source
import okio.buffer
import okio.use
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

suspend fun Source.saveTo(path: Path) {
    withIOContext {
        use { source ->
            FileSystem.SYSTEM
                .sink(path)
                .buffer()
                .use { it.writeAll(source) }
        }
    }
}

suspend fun Source.copyTo(sink: BufferedSink) {
    withIOContext {
        use { source ->
            sink.use { it.writeAll(source) }
        }
    }
}

fun ByteArray.source(): BufferedSource = Buffer().write(this)

expect suspend fun ByteReadChannel.toSource(context: CoroutineContext = EmptyCoroutineContext): Source
