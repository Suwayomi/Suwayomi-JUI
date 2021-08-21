/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.util.system

import ca.gosyer.BuildConfig
import ca.gosyer.util.lang.launchUI
import kotlinx.coroutines.DelicateCoroutinesApi
import mu.KotlinLogging
import net.harawata.appdirs.AppDirs
import net.harawata.appdirs.AppDirsFactory
import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

private val logger = KotlinLogging.logger {}

val appDirs: AppDirs by lazy {
    AppDirsFactory.getInstance()
}

val userDataDir: File by lazy {
    File(appDirs.getUserDataDir(BuildConfig.NAME, null, null)).also {
        if (!it.exists()) {
            logger.info("Attempted to create app data dir, result: {}", it.mkdirs())
        }
    }
}

fun filePicker(
    vararg extensions: String,
    builder: JFileChooser.() -> Unit = {},
    onCancel: (JFileChooser) -> Unit = {},
    onError: (JFileChooser) -> Unit = {},
    onApprove: (JFileChooser) -> Unit
) = fileChooser(false, builder, onCancel, onError, onApprove, extensions = extensions)

fun fileSaver(
    defaultFileName: String,
    extension: String,
    builder: JFileChooser.() -> Unit = {},
    onCancel: (JFileChooser) -> Unit = {},
    onError: (JFileChooser) -> Unit = {},
    onApprove: (JFileChooser) -> Unit
) = fileChooser(
    true,
    builder,
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
 * @param builder invokes this builder before launching the file picker, such as adding a action listener
 * @param onCancel the listener that is called when picking a file is canceled
 * @param onError the listener that is called when picking a file exited with a error
 * @param onApprove the listener that is called when picking a file is completed
 */
@OptIn(DelicateCoroutinesApi::class)
private fun fileChooser(
    saving: Boolean = false,
    builder: JFileChooser.() -> Unit = {},
    onCancel: (JFileChooser) -> Unit = {},
    onError: (JFileChooser) -> Unit = {},
    onApprove: (JFileChooser) -> Unit,
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
                selectedFile = File(defaultFileName)
            }
        }
        .apply(builder)

    val result = fileChooser.let {
        if (saving) {
            it.showSaveDialog(null)
        } else {
            it.showOpenDialog(null)
        }
    }

    when (result) {
        JFileChooser.APPROVE_OPTION -> onApprove(fileChooser)
        JFileChooser.CANCEL_OPTION -> onCancel(fileChooser)
        JFileChooser.ERROR_OPTION -> onError(fileChooser)
    }
}
