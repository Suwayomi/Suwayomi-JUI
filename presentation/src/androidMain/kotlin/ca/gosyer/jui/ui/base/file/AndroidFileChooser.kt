/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.base.file

import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import okio.Source
import okio.source

actual class FileChooser(
    private val resultLauncher: ManagedActivityResultLauncher<Array<String>, Uri?>,
) {
    actual fun launch(vararg extensions: String) {
        val mimes = extensions.mapNotNull { extension ->
            MimeTypeMap.getSingleton()
                .getMimeTypeFromExtension(extension)
        }.toTypedArray()
        resultLauncher.launch(mimes)
    }
}

@Composable
actual fun rememberFileChooser(onFileFound: (Source) -> Unit): FileChooser {
    val context = LocalContext.current
    val result = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) {
        if (it != null) {
            context.contentResolver.openInputStream(it)?.source()?.let(onFileFound)
        }
    }

    return remember { FileChooser(result) }
}
