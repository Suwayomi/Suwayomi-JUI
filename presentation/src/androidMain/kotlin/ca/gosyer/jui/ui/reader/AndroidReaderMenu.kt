/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.reader

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

actual class ReaderLauncher(
    private val context: Context,
) {
    actual fun launch(
        chapterId: Long,
        mangaId: Long,
    ) {
        Intent(context, Class.forName("ca.gosyer.jui.android.ReaderActivity")).apply {
            putExtra("manga", mangaId)
            putExtra("chapter", chapterId)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        }.let(context::startActivity)
    }

    @Composable
    actual fun Reader() {
    }
}

@Composable
actual fun rememberReaderLauncher(): ReaderLauncher {
    val context = LocalContext.current
    return remember(context) { ReaderLauncher(context) }
}
