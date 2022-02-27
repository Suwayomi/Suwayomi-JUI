/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.base.file

import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.core.net.toFile
import okio.Path
import okio.Path.Companion.toOkioPath

actual class FileChooser(private val resultLauncher: ManagedActivityResultLauncher<String, Uri?>) {
    actual fun launch(extension: String) {
        resultLauncher.launch(MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension))
    }
}

@Composable
actual fun rememberFileChooser(onFileFound: (Path) -> Unit): FileChooser {
    val result = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
        it?.toFile()?.toOkioPath()?.let(onFileFound)
    }

    return remember { FileChooser(result) }
}