/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.common.util

import okio.ByteString.Companion.decodeBase64
import okio.ByteString.Companion.encode

fun String.decodeBase64() = decodeBase64()!!

fun String.md5() = encode().md5().hex()
