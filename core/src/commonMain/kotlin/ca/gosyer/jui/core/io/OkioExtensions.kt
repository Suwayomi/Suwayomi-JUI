/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.core.io

import ca.gosyer.jui.core.lang.withIOContext
import okio.BufferedSink
import okio.FileSystem
import okio.Path
import okio.Source
import okio.buffer
import okio.use

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
