/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.base.file

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import ca.gosyer.core.lang.launchDefault
import kotlinx.coroutines.CoroutineScope
import okio.Path
import okio.Path.Companion.toOkioPath
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

actual class FileChooser(private val onFileFound: (Path) -> Unit, private val scope: CoroutineScope) {
    private val fileChooser = JFileChooser()
        .apply {
            val details = actionMap.get("viewTypeDetails")
            details?.actionPerformed(null)

        }

    actual fun launch(extension: String) {
        scope.launchDefault {
            fileChooser.fileFilter = FileNameExtensionFilter("$extension file", extension)
            when (fileChooser.showOpenDialog(null)) {
                JFileChooser.APPROVE_OPTION -> onFileFound(fileChooser.selectedFile.toOkioPath())
            }
        }
    }
}

@Composable
actual fun rememberFileChooser(onFileFound: (Path) -> Unit): FileChooser {
    val coroutineScope = rememberCoroutineScope()
    return remember { FileChooser(onFileFound, coroutineScope)  }
}