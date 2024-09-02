/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.android

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.CompositionLocalProvider
import ca.gosyer.jui.ui.base.theme.AppTheme
import ca.gosyer.jui.ui.reader.ReaderMenu

class ReaderActivity : AppCompatActivity() {
    companion object {
        fun newIntent(
            context: Context,
            mangaId: Long,
            chapterId: Long,
        ): Intent =
            Intent(context, ReaderActivity::class.java).apply {
                putExtra("manga", mangaId)
                putExtra("chapter", chapterId)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val hooks = AppComponent.getInstance(applicationContext).hooks

        val mangaId = intent.extras!!.getLong("manga", -1)
        val chapterId = intent.extras!!.getLong("chapter", -1)
        if (mangaId == -1L || chapterId == -1L) {
            finish()
            return
        }

        setContent {
            CompositionLocalProvider(*hooks) {
                AppTheme {
                    ReaderMenu(
                        chapterId = chapterId,
                        mangaId = mangaId,
                        onCloseRequest = onBackPressedDispatcher::onBackPressed,
                    )
                }
            }
        }
    }
}
