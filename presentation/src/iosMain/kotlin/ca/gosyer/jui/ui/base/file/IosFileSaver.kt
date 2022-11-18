/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.base.file

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import okio.Sink

actual class FileSaver(
    private val onFileSelected: (Sink) -> Unit,
    private val onCancel: () -> Unit,
    private val onError: () -> Unit,
) {
    actual fun save(name: String) {
        TODO()
    }
}

@Composable
actual fun rememberFileSaver(
    onFileSelected: (Sink) -> Unit,
    onCancel: () -> Unit,
    onError: () -> Unit
): FileSaver {
    return remember { FileSaver(onFileSelected, onCancel, onError) }
}
