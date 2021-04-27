/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.common.io

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.BufferedSink
import okio.Source
import okio.buffer
import okio.sink
import java.io.File

suspend fun Source.saveTo(file: File) {
    withContext(Dispatchers.IO) {
        use { source ->
            file.sink().buffer().use { it.writeAll(source) }
        }
    }
}

suspend fun Source.copyTo(sink: BufferedSink) {
    withContext(Dispatchers.IO) {
        use { source ->
            sink.use { it.writeAll(source) }
        }
    }
}
