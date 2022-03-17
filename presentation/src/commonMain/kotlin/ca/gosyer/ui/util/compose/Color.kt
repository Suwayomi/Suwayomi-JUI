/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.util.compose

import androidx.compose.ui.graphics.Color

val Long.color get() = Color(this)

expect fun Color.toHexString(): String

expect fun Color.toLong(): Long