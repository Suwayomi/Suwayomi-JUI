/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.util.lang

import io.ktor.utils.io.ByteReadChannel
import okio.Source

expect fun ByteReadChannel.toSource(): Source