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
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.NativeKeyEvent
import androidx.compose.ui.input.key.key
import androidx.lifecycle.lifecycleScope
import ca.gosyer.ui.AppComponent
import ca.gosyer.ui.base.theme.AppTheme
import ca.gosyer.ui.reader.ReaderMenu
import ca.gosyer.ui.reader.supportedKeyList
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

class ReaderActivity : AppCompatActivity() {

    companion object {
        fun newIntent(context: Context, mangaId: Long, chapterIndex: Int): Intent {
            return Intent(context, ReaderActivity::class.java).apply {
                putExtra("manga", mangaId)
                putExtra("chapter", chapterIndex)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
        }
    }

    private val hotkeyFlow = MutableSharedFlow<KeyEvent>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val hooks = AppComponent.getInstance(applicationContext).uiComponent.getHooks()

        val mangaId = intent.extras!!.getLong("manga", -1)
        val chapterIndex = intent.extras!!.getInt("chapter", -1)
        if (mangaId == -1L || chapterIndex == -1) {
            finish()
            return
        }

        setContent {
            CompositionLocalProvider(
                *hooks
            ) {
                AppTheme {
                    ReaderMenu(chapterIndex, mangaId, hotkeyFlow)
                }
            }
        }
    }

    override fun onKeyUp(keyCode: Int, event: android.view.KeyEvent?): Boolean {
        event ?: super.onKeyUp(keyCode, event)
        val composeKeyEvent = KeyEvent(event as NativeKeyEvent)
        lifecycleScope.launch {
            hotkeyFlow.emit(composeKeyEvent)
        }

        return if (composeKeyEvent.key in supportedKeyList) {
            true
        } else {
            super.onKeyUp(keyCode, event)
        }
    }
}