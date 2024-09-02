/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.util.lang

import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.jvm.javaio.toInputStream
import okio.Source
import okio.source
import kotlin.coroutines.CoroutineContext

actual suspend fun ByteReadChannel.toSource(context: CoroutineContext): Source = toInputStream().source()
