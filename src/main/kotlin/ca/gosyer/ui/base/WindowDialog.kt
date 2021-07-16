/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.base

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.key
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowSize
import androidx.compose.ui.window.rememberWindowState
import ca.gosyer.common.di.AppScope
import ca.gosyer.data.translation.XmlResourceBundle
import ca.gosyer.ui.base.resources.LocalResources
import ca.gosyer.ui.base.theme.AppTheme
import ca.gosyer.util.lang.launchApplication
import kotlinx.coroutines.DelicateCoroutinesApi

@OptIn(DelicateCoroutinesApi::class)
@Suppress("FunctionName")
fun WindowDialog(
    title: String = "Dialog",
    size: WindowSize = WindowSize(400.dp, 200.dp),
    onCloseRequest: (() -> Unit)? = null,
    forceFocus: Boolean = true,
    showNegativeButton: Boolean = true,
    negativeButtonText: String = "Cancel",
    onNegativeButton: (() -> Unit)? = null,
    positiveButtonText: String = "OK",
    onPositiveButton: (() -> Unit)? = null,
    keyboardShortcuts: Map<Key, (KeyEvent) -> Boolean> = emptyMap(),
    row: @Composable (RowScope.() -> Unit)
) = launchApplication {
    DisposableEffect(Unit) {
        onDispose {
            onCloseRequest?.invoke()
        }
    }

    fun (() -> Unit)?.plusClose(): (() -> Unit) = {
        this?.invoke()
        exitApplication()
    }

    val resources = remember { AppScope.getInstance<XmlResourceBundle>() }
    val windowState = rememberWindowState(size = size, position = WindowPosition(Alignment.Center))

    Window(
        title = title,
        state = windowState,
        onCloseRequest = ::exitApplication,
        onKeyEvent = {
            when {
                it.key == Key.Enter -> {
                    onPositiveButton.plusClose()()
                    true
                }
                it.key == Key.Escape -> {
                    onNegativeButton.plusClose()()
                    true
                }
                keyboardShortcuts[it.key] != null -> {
                    keyboardShortcuts[it.key]?.invoke(it) ?: false
                }
                else -> false
            }
        },
    ) {
        CompositionLocalProvider(
            LocalResources provides resources
        ) {
            AppTheme {
                Surface {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Row(
                            content = row,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.End,
                            modifier = Modifier.height(70.dp)
                                .align(Alignment.BottomEnd)
                        ) {
                            if (showNegativeButton) {
                                OutlinedButton(onNegativeButton.plusClose(), modifier = Modifier.padding(end = 8.dp, bottom = 8.dp)) {
                                    Text(negativeButtonText)
                                }
                            }

                            OutlinedButton(onPositiveButton.plusClose(), modifier = Modifier.padding(end = 8.dp, bottom = 8.dp)) {
                                Text(positiveButtonText)
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(DelicateCoroutinesApi::class)
fun WindowDialog(
    title: String = "Dialog",
    size: WindowSize = WindowSize(400.dp, 200.dp),
    onCloseRequest: (() -> Unit)? = null,
    forceFocus: Boolean = true,
    keyboardShortcuts: Map<Key, (KeyEvent) -> Boolean> = emptyMap(),
    buttons: @Composable (() -> Unit) -> Unit,
    content: @Composable (() -> Unit) -> Unit
) = launchApplication {
    DisposableEffect(Unit) {
        onDispose {
            onCloseRequest?.invoke()
        }
    }

    val resources = remember { AppScope.getInstance<XmlResourceBundle>() }
    val windowState = rememberWindowState(size = size)

    Window(
        title = title,
        state = windowState,
        onCloseRequest = ::exitApplication,
        onKeyEvent = {
            when {
                keyboardShortcuts[it.key] != null -> {
                    keyboardShortcuts[it.key]?.invoke(it) ?: false
                }
                else -> false
            }
        }
    ) {
        CompositionLocalProvider(
            LocalResources provides resources
        ) {
            AppTheme {
                Surface {
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        BoxWithConstraints {
                            content(::exitApplication)
                            buttons(::exitApplication)
                        }
                    }
                }
            }
        }
    }
}
