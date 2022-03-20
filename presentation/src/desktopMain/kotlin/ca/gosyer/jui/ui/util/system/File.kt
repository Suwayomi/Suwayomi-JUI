/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.util.system

import ca.gosyer.jui.core.lang.launchUI
import kotlinx.coroutines.DelicateCoroutinesApi
import okio.Path
import okio.Path.Companion.toOkioPath
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter
import kotlin.io.path.Path

fun filePicker(
    vararg extensions: String,
    onCancel: () -> Unit = {},
    onError: () -> Unit = {},
    onApprove: (Path) -> Unit
) = fileChooser(false, onCancel, onError, onApprove, extensions = extensions)

fun fileSaver(
    defaultFileName: String,
    extension: String,
    onCancel: () -> Unit = {},
    onError: () -> Unit = {},
    onApprove: (Path) -> Unit
) = fileChooser(
    true,
    onCancel,
    onError,
    onApprove,
    defaultFileName,
    extension
)

/**
 * Opens a swing file picker, in the details view by default
 *
 * @param saving true if the dialog is going to save a file, false if its going to open a file
 * @param onCancel the listener that is called when picking a file is canceled
 * @param onError the listener that is called when picking a file exited with a error
 * @param onApprove the listener that is called when picking a file is completed
 */
@OptIn(DelicateCoroutinesApi::class)
internal fun fileChooser(
    saving: Boolean = false,
    onCancel: () -> Unit = {},
    onError: () -> Unit = {},
    onApprove: (Path) -> Unit,
    defaultFileName: String = "",
    vararg extensions: String,
) = launchUI {
    val fileChooser = JFileChooser()
        .apply {
            val details = actionMap.get("viewTypeDetails")
            details?.actionPerformed(null)
            if (extensions.isNotEmpty()) {
                fileFilter = FileNameExtensionFilter("${extensions.joinToString()} files", *extensions)
            }
            if (saving) {
                selectedFile = Path(defaultFileName).toFile()
            }
        }

    val result = fileChooser.let {
        if (saving) {
            it.showSaveDialog(null)
        } else {
            it.showOpenDialog(null)
        }
    }

    when (result) {
        JFileChooser.APPROVE_OPTION -> onApprove(fileChooser.selectedFile.toOkioPath())
        JFileChooser.CANCEL_OPTION -> onCancel()
        JFileChooser.ERROR_OPTION -> onError()
    }
}
