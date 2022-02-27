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
import androidx.core.net.toFile
import okio.Path
import okio.Path.Companion.toOkioPath

actual class FileSaver(
    private val resultLauncher: ManagedActivityResultLauncher<String, Uri?>,
) {
    actual fun save(name: String) {
        resultLauncher.launch(name)
    }
}

@Composable
actual fun rememberFileSaver(
    onFileSelected: (Path) -> Unit,
    onCancel: () -> Unit,
    onError: () -> Unit,
): FileSaver {
    val result = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument()) {
        if (it != null) {
            it.toFile().toOkioPath().let(onFileSelected)
        } else {
            onCancel()
        }
    }

    return remember { FileSaver(result) }
}