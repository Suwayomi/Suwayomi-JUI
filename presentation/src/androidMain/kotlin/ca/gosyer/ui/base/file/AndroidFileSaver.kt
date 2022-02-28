/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.base.file

import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import okio.Sink
import okio.sink

actual class FileSaver(
    private val resultLauncher: ManagedActivityResultLauncher<String, Uri?>,
) {
    actual fun save(name: String) {
        resultLauncher.launch(name)
    }
}

@Composable
actual fun rememberFileSaver(
    onFileSelected: (Sink) -> Unit,
    onCancel: () -> Unit,
    onError: () -> Unit,
): FileSaver {
    val context = LocalContext.current
    val result = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument()) {
        if (it != null) {
            context.contentResolver.openOutputStream(it)?.sink()?.let(onFileSelected)
        } else {
            onCancel()
        }
    }

    return remember { FileSaver(result) }
}