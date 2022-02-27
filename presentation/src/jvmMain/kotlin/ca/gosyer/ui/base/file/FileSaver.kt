/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.base.file

import androidx.compose.runtime.Composable
import okio.Path

expect class FileSaver {
    fun save(name: String)
}

@Composable
expect fun rememberFileSaver(
    onFileSelected: (Path) -> Unit,
    onCancel: () -> Unit = {},
    onError: () -> Unit = {},
): FileSaver