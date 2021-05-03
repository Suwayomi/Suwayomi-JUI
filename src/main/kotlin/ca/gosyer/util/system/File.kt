/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.util.system

import ca.gosyer.BuildConfig
import net.harawata.appdirs.AppDirs
import net.harawata.appdirs.AppDirsFactory
import java.io.File
import javax.swing.JFileChooser
import javax.swing.SwingUtilities

val appDirs: AppDirs by lazy {
    AppDirsFactory.getInstance()
}

val userDataDir: File by lazy {
    File(appDirs.getUserDataDir(BuildConfig.NAME, null, null)).also {
        if (!it.exists()) it.mkdirs()
    }
}

/**
 * Opens a swing file picker, in the details view by default
 *
 * @param builder invokes this builder before launching the file picker, such as adding a action listener
 * @param onCancel the listener that is called when picking a file is canceled
 * @param onError the listener that is called when picking a file exited with a error
 * @param onApprove the listener that is called when picking a file is completed
 */
fun filePicker(
    builder: JFileChooser.() -> Unit = {},
    onCancel: (JFileChooser) -> Unit = {},
    onError: (JFileChooser) -> Unit = {},
    onApprove: (JFileChooser) -> Unit
) = SwingUtilities.invokeLater {
    val fileChooser = JFileChooser()
        .apply {
            val details = actionMap.get("viewTypeDetails")
            details?.actionPerformed(null)
        }
        .apply(builder)

    val result = fileChooser
        .showOpenDialog(null)

    when (result) {
        JFileChooser.APPROVE_OPTION -> onApprove(fileChooser)
        JFileChooser.CANCEL_OPTION -> onCancel(fileChooser)
        JFileChooser.ERROR_OPTION -> onError(fileChooser)
    }
}
