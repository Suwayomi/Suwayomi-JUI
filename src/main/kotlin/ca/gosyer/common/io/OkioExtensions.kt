/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.common.io

import ca.gosyer.util.lang.withIOContext
import okio.BufferedSink
import okio.Source
import okio.buffer
import okio.sink
import java.io.File

suspend fun Source.saveTo(file: File) {
    withIOContext {
        use { source ->
            file.sink().buffer().use { it.writeAll(source) }
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
