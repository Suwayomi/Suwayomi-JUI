package ca.gosyer.jui.uicore.components

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalFocusManager

/**
 * A modifier to handle keyboard keys properly
 */
@OptIn(ExperimentalComposeUiApi::class)
fun Modifier.keyboardHandler(
    singleLine: Boolean = false,
    enterAction: ((FocusManager) -> Unit)? = null,
    action: (FocusManager) -> Unit = { it.moveFocus(FocusDirection.Down) }
) = composed {
    val focusManager = LocalFocusManager.current
    Modifier.onPreviewKeyEvent {
        if (it.type == KeyEventType.KeyDown) {
            if (singleLine && (it.key == Key.Enter || it.key == Key.NumPadEnter)) {
                enterAction?.invoke(focusManager) ?: action(focusManager)
                true
            } else if (it.key == Key.Tab) {
                action(focusManager)
                true
            } else {
                false
            }
        } else {
            false
        }
    }
}
