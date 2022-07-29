/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.base.file

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import ca.gosyer.jui.core.lang.launchDefault
import kotlinx.coroutines.CoroutineScope
import okio.Sink
import okio.sink
import javax.swing.JFileChooser

actual class FileSaver(
    private val onFileSelected: (Sink) -> Unit,
    private val onCancel: () -> Unit,
    private val onError: () -> Unit,
    private val scope: CoroutineScope
) {
    private val fileChooser = JFileChooser()
        .apply {
            val details = actionMap.get("viewTypeDetails")
            details?.actionPerformed(null)
        }

    actual fun save(name: String) {
        scope.launchDefault {
            fileChooser.selectedFile = fileChooser.currentDirectory.resolve(name)
            when (fileChooser.showSaveDialog(null)) {
                JFileChooser.APPROVE_OPTION -> onFileSelected(fileChooser.selectedFile.sink())
                JFileChooser.CANCEL_OPTION -> onCancel()
                JFileChooser.ERROR_OPTION -> onError()
            }
        }
    }
}

@Composable
actual fun rememberFileSaver(
    onFileSelected: (Sink) -> Unit,
    onCancel: () -> Unit,
    onError: () -> Unit
): FileSaver {
    val coroutineScope = rememberCoroutineScope()
    return remember { FileSaver(onFileSelected, onCancel, onError, coroutineScope) }
}
