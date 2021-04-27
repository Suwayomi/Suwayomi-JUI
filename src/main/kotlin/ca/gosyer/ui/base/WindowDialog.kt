/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.base

import androidx.compose.desktop.AppWindow
import androidx.compose.desktop.WindowEvents
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import ca.gosyer.ui.base.theme.AppTheme
import javax.swing.SwingUtilities

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
    row: @Composable (RowScope.() -> Unit)
) = SwingUtilities.invokeLater {
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

    window.keyboard.setShortcut(Key.Enter, onPositiveButton.plusClose())
    window.keyboard.setShortcut(Key.Escape, onNegativeButton.plusClose())

    window.show {
        AppTheme {
            Surface {
                Column(verticalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxSize(),) {
                    Row(content = row, modifier = Modifier.fillMaxWidth())
                    Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.End, modifier = Modifier.requiredHeight(70.dp).fillMaxWidth()) {
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

fun WindowDialog(
    title: String = "Dialog",
    size: IntSize = IntSize(400, 200),
    onDismissRequest: (() -> Unit)? = null,
    forceFocus: Boolean = true,
    buttons: @Composable (AppWindow) -> Unit,
    content: @Composable (AppWindow) -> Unit
) = SwingUtilities.invokeLater {
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

    window.show {
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
