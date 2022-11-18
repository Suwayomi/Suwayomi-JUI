/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.base.file

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import okio.Source

actual class FileChooser(private val onFileFound: (Source) -> Unit) {
    actual fun launch(extension: String) {
        TODO()
    }
}

@Composable
actual fun rememberFileChooser(onFileFound: (Source) -> Unit): FileChooser {
    return remember { FileChooser(onFileFound) }
}
