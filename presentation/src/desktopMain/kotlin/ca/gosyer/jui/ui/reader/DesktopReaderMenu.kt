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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberWindowState
import ca.gosyer.jui.presentation.build.BuildKonfig
import ca.gosyer.jui.ui.util.lang.launchApplication
import kotlinx.coroutines.DelicateCoroutinesApi

actual class ReaderLauncher {
    private var isOpen by mutableStateOf<Pair<Long, Long>?>(null)

    actual fun launch(
        chapterId: Long,
        mangaId: Long,
    ) {
        isOpen = chapterId to mangaId
    }

    @OptIn(DelicateCoroutinesApi::class)
    @Composable
    actual fun Reader() {
        val localParams = currentCompositionLocalContext
        DisposableEffect(isOpen) {
            isOpen?.let { (chapterId, mangaId) ->
                launchApplication {
                    val windowState = rememberWindowState(
                        position = WindowPosition.Aligned(Alignment.Center),
                    )
                    val icon = painterResource("icon.png")
                    CompositionLocalProvider(localParams) {
                        Window(
                            onCloseRequest = ::exitApplication,
                            title = "${BuildKonfig.NAME} - Reader",
                            icon = icon,
                            state = windowState,
                        ) {
                            ReaderMenu(
                                chapterId = chapterId,
                                mangaId = mangaId,
                                onCloseRequest = ::exitApplication,
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
actual fun rememberReaderLauncher(): ReaderLauncher = remember { ReaderLauncher() }
