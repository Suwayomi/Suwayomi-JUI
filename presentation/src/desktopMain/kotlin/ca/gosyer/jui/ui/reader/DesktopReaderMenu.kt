/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.reader

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.currentCompositionLocalContext
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberWindowState
import ca.gosyer.jui.presentation.build.BuildKonfig
import ca.gosyer.jui.ui.base.model.StableHolder
import ca.gosyer.jui.ui.util.lang.launchApplication
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

actual class ReaderLauncher {

    private var isOpen by mutableStateOf<Pair<Int, Long>?>(null)

    actual fun launch(
        chapterIndex: Int,
        mangaId: Long
    ) {
        isOpen = chapterIndex to mangaId
    }

    @OptIn(DelicateCoroutinesApi::class)
    @Composable
    actual fun Reader() {
        val localParams = currentCompositionLocalContext
        DisposableEffect(isOpen) {
            isOpen?.let { (chapterIndex, mangaId) ->
                launchApplication {
                    val scope = rememberCoroutineScope()
                    val hotkeyFlow = remember { MutableSharedFlow<KeyEvent>() }
                    val hotkeyFlowHolder = remember { StableHolder(hotkeyFlow.asSharedFlow()) }
                    val windowState = rememberWindowState(
                        position = WindowPosition.Aligned(Alignment.Center)
                    )
                    val icon = painterResource("icon.png")
                    CompositionLocalProvider(localParams) {
                        Window(
                            onCloseRequest = ::exitApplication,
                            title = "${BuildKonfig.NAME} - Reader",
                            icon = icon,
                            state = windowState,
                            onKeyEvent = {
                                if (it.type != KeyEventType.KeyDown) return@Window false
                                scope.launch {
                                    hotkeyFlow.emit(it)
                                }
                                it.key in supportedKeyList
                            }
                        ) {
                            ReaderMenu(
                                chapterIndex = chapterIndex,
                                mangaId = mangaId,
                                hotkeyFlowHolder = hotkeyFlowHolder,
                                onCloseRequest = ::exitApplication
                            )
                        }
                    }
                }
            }
            isOpen = null
            onDispose {}
        }
    }
}

@Composable
actual fun rememberReaderLauncher(): ReaderLauncher {
    return remember { ReaderLauncher() }
}
