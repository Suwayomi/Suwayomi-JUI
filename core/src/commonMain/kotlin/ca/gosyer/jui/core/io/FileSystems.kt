/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.core.io

import okio.FileSystem

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
expect val FileSystem.Companion.SYSTEM: FileSystem
