/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.util.lang

import ca.gosyer.jui.core.io.source
import io.ktor.util.toByteArray
import io.ktor.utils.io.ByteReadChannel
import okio.Source

actual suspend fun ByteReadChannel.toSource(): Source {
    return this.toByteArray().source()
}
