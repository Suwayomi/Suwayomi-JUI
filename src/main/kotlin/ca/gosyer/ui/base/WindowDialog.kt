/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.base

import androidx.compose.desktop.AppWindow
import androidx.compose.desktop.WindowEvents
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.key
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import ca.gosyer.common.di.AppScope
import ca.gosyer.data.translation.XmlResourceBundle
import ca.gosyer.ui.base.resources.LocalResources
import ca.gosyer.ui.base.theme.AppTheme
import ca.gosyer.util.lang.launchUI
import kotlinx.coroutines.DelicateCoroutinesApi

@OptIn(DelicateCoroutinesApi::class)
@Suppress("FunctionName")
fun WindowDialog(
    title: String = "Dialog",
    size: IntSize = IntSize(400, 200),
    onDismissRequest: (() -> Unit)? = null,
    forceFocus: Boolean = true,
    showNegativeButton: Boolean = true,
    negativeButtonText: String = "Cancel",
    onNegativeButton: (() -> Unit)? = null,
    positiveButtonText: String = "OK",
    onPositiveButton: (() -> Unit)? = null,
    keyboardShortcuts: Map<Key, (KeyEvent, AppWindow) -> Boolean> = emptyMap(),
    row: @Composable (RowScope.() -> Unit)
) = launchUI {
    val window = AppWindow(
        title = title,
        size = size,
        location = IntOffset.Zero,
        centered = true,
        icon = null,
        menuBar = null,
        undecorated = false,
        events = WindowEvents(),
        onDismissRequest = onDismissRequest
    )

    if (forceFocus) {
        window.events.onFocusLost = {
            window.window.requestFocus()
        }
    }

    fun (() -> Unit)?.plusClose(): (() -> Unit) = {
        this?.invoke()
        window.close()
    }

    window.keyboard.onKeyEvent = {
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
                keyboardShortcuts[it.key]?.invoke(it, window) ?: false
            }
            else -> false
        }
    }

    val resources = AppScope.getInstance<XmlResourceBundle>()

    window.show {
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
    size: IntSize = IntSize(400, 200),
    onDismissRequest: (() -> Unit)? = null,
    forceFocus: Boolean = true,
    keyboardShortcuts: Map<Key, (KeyEvent, AppWindow) -> Boolean> = emptyMap(),
    buttons: @Composable (AppWindow) -> Unit,
    content: @Composable (AppWindow) -> Unit
) = launchUI {
    val window = AppWindow(
        title = title,
        size = size,
        location = IntOffset.Zero,
        centered = true,
        icon = null,
        menuBar = null,
        undecorated = false,
        events = WindowEvents(),
        onDismissRequest = onDismissRequest
    )

    if (forceFocus) {
        window.events.onFocusLost = {
            window.window.requestFocus()
        }
    }

    window.keyboard.onKeyEvent = {
        when {
            keyboardShortcuts[it.key] != null -> {
                keyboardShortcuts[it.key]?.invoke(it, window) ?: false
            }
            else -> false
        }
    }

    val resources = AppScope.getInstance<XmlResourceBundle>()

    window.show {
        CompositionLocalProvider(
            LocalResources provides resources
        ) {
            AppTheme {
                Surface {
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        content(window)
                        buttons(window)
                    }
                }
            }
        }
    }
}
