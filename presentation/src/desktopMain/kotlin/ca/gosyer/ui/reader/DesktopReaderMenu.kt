/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.reader

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.rememberWindowState
import ca.gosyer.data.ui.model.WindowSettings
import ca.gosyer.presentation.build.BuildKonfig
import ca.gosyer.ui.AppComponent
import ca.gosyer.ui.base.theme.AppTheme
import ca.gosyer.ui.util.compose.WindowGet
import ca.gosyer.ui.util.lang.launchApplication
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

actual class ReaderLauncher {
    actual fun launch(
        chapterIndex: Int,
        mangaId: Long
    ) {
        openReaderMenu(chapterIndex, mangaId)
    }
}

@Composable
actual fun rememberReaderLauncher(): ReaderLauncher {
    return remember { ReaderLauncher() }
}

@OptIn(DelicateCoroutinesApi::class)
fun openReaderMenu(chapterIndex: Int, mangaId: Long) {
    val windowSettings = AppComponent.getInstance().dataComponent.uiPreferences
        .readerWindow()
    val (
        position,
        size,
        placement
    ) = WindowGet.from(windowSettings.get())

    val hooks = AppComponent.getInstance().uiComponent.getHooks()

    launchApplication {
        val scope = rememberCoroutineScope()
        val hotkeyFlow = remember { MutableSharedFlow<KeyEvent>() }
        val icon = painterResource("icon.png")
        val windowState = rememberWindowState(size = size, position = position, placement = placement)
        DisposableEffect(Unit) {
            onDispose {
                windowSettings.set(
                    WindowSettings(
                        windowState.position.x.value.toInt(),
                        windowState.position.y.value.toInt(),
                        windowState.size.width.value.toInt(),
                        windowState.size.height.value.toInt(),
                        windowState.placement == WindowPlacement.Maximized,
                        windowState.placement == WindowPlacement.Fullscreen
                    )
                )
            }
        }
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
            CompositionLocalProvider(
                *hooks
            ) {
                AppTheme {
                    ReaderMenu(chapterIndex, mangaId, hotkeyFlow)
                }
            }
        }
    }
}
