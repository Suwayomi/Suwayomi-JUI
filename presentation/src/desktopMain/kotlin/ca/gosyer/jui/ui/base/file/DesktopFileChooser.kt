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
import okio.Source
import okio.source
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

actual class FileChooser(
    private val onFileFound: (Source) -> Unit,
    private val scope: CoroutineScope,
) {
    private val fileChooser = JFileChooser()
        .apply {
            val details = actionMap.get("viewTypeDetails")
            details?.actionPerformed(null)
        }

    actual fun launch(vararg extensions: String) {
        scope.launchDefault {
            fileChooser.fileFilter = FileNameExtensionFilter("${extensions.joinToString()} files", *extensions)
            when (fileChooser.showOpenDialog(null)) {
                JFileChooser.APPROVE_OPTION -> onFileFound(fileChooser.selectedFile.source())
            }
        }
    }
}

@Composable
actual fun rememberFileChooser(onFileFound: (Source) -> Unit): FileChooser {
    val coroutineScope = rememberCoroutineScope()
    return remember { FileChooser(onFileFound, coroutineScope) }
}
