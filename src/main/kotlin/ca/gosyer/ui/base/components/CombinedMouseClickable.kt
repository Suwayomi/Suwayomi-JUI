/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.base.components

import androidx.compose.foundation.ExperimentalDesktopApi
import androidx.compose.foundation.Indication
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.MouseClickScope
import androidx.compose.foundation.awaitEventFirstDown
import androidx.compose.foundation.gestures.forEachGesture
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material.CursorDropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.input.pointer.AwaitPointerEventScope
import androidx.compose.ui.input.pointer.PointerButtons
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.PointerKeyboardModifiers
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.consumeDownChange
import androidx.compose.ui.input.pointer.isOutOfBounds
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ContextMenuItem
import androidx.compose.ui.platform.debugInspectorInfo
import androidx.compose.ui.semantics.AccessibilityAction
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsPropertyKey
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import kotlinx.coroutines.coroutineScope

@OptIn(ExperimentalDesktopApi::class)
fun Modifier.contextMenuClickable(
    items: () -> List<ContextMenuItem>,
    onClickLabel: String? = null,
    onMiddleClickLabel: String? = null,
    onRightClickLabel: String? = null,
    onClick: MouseClickScope.(IntOffset) -> Unit = {},
    onMiddleClick: MouseClickScope.(IntOffset) -> Unit = {},
    enabled: Boolean = true
) = composed(
    inspectorInfo = debugInspectorInfo {
        name = "contextMenuClickable"
        properties["onClick"] = onClick
        properties["onMiddleClick"] = onMiddleClick
        properties["enabled"] = enabled
        properties["onClickLabel"] = onClickLabel
        properties["onMiddleClickLabel"] = onMiddleClickLabel
        properties["onRightClickLabel"] = onRightClickLabel
    }
) {
    var expanded by remember { mutableStateOf(false) }
    CursorDropdownMenu(
        expanded,
        onDismissRequest = { expanded = false }
    ) {
        items().forEach { item ->
            DropdownMenuItem(
                onClick = {
                    expanded = false
                    item.onClick()
                }
            ) {
                Text(text = item.label)
            }
        }
    }
    Modifier.combinedMouseClickable(
        onClick = onClick,
        onMiddleClick = onMiddleClick,
        onClickLabel = onClickLabel,
        onMiddleClickLabel = onMiddleClickLabel,
        onRightClickLabel = onRightClickLabel,
        onRightClick = { expanded = true },
        enabled = enabled
    )
}

@OptIn(ExperimentalDesktopApi::class)
fun Modifier.combinedMouseClickable(
    rightClickIsContextMenu: Boolean = true,
    onClick: MouseClickScope.(IntOffset) -> Unit = {},
    onMiddleClick: MouseClickScope.(IntOffset) -> Unit = {},
    onRightClick: MouseClickScope.(IntOffset) -> Unit = {},
    onClickLabel: String? = null,
    onMiddleClickLabel: String? = null,
    onRightClickLabel: String? = null,
    enabled: Boolean = true
) = composed(
    inspectorInfo = debugInspectorInfo {
        name = "combinedMouseClickable"
        properties["rightClickIsContextMenu"] = rightClickIsContextMenu
        properties["onClick"] = onClick
        properties["onMiddleClick"] = onMiddleClick
        properties["onRightClick"] = onRightClick
        properties["enabled"] = enabled
        properties["onClickLabel"] = onClickLabel
        properties["onMiddleClickLabel"] = onMiddleClickLabel
        properties["onRightClickLabel"] = onRightClickLabel
    }
) {
    Modifier.combinedMouseClickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = LocalIndication.current,
        rightClickIsContextMenu = rightClickIsContextMenu,
        onClick = onClick,
        onMiddleClick = onMiddleClick,
        onRightClick = onRightClick,
        onClickLabel = onClickLabel,
        onMiddleClickLabel = onMiddleClickLabel,
        onRightClickLabel = onRightClickLabel,
        enabled = enabled
    )
}

@OptIn(ExperimentalDesktopApi::class)
fun Modifier.combinedMouseClickable(
    interactionSource: MutableInteractionSource,
    indication: Indication?,
    enabled: Boolean = true,
    onClickLabel: String? = null,
    onMiddleClickLabel: String? = null,
    onRightClickLabel: String? = null,
    role: Role? = null,
    rightClickIsContextMenu: Boolean = true,
    onClick: MouseClickScope.(IntOffset) -> Unit,
    onMiddleClick: MouseClickScope.(IntOffset) -> Unit,
    onRightClick: MouseClickScope.(IntOffset) -> Unit
) = composed(
    inspectorInfo = debugInspectorInfo {
        name = "combinedMouseClickable"
        properties["enabled"] = enabled
        properties["onClickLabel"] = onClickLabel
        properties["onMiddleClickLabel"] = onMiddleClickLabel
        properties["onRightClickLabel"] = onRightClickLabel
        properties["role"] = role
        properties["rightClickIsContextMenu"] = rightClickIsContextMenu
        properties["onClick"] = onClick
        properties["onMiddleClick"] = onMiddleClick
        properties["onRightClick"] = onRightClick
        properties["indication"] = indication
        properties["interactionSource"] = interactionSource
    }
) {
    val onClickState = rememberUpdatedState(onClick)
    val onMiddleClickState = rememberUpdatedState(onMiddleClick)
    val onRightClickState = rememberUpdatedState(onRightClick)
    val gesture = if (enabled) {
        Modifier.pointerInput(Unit) {
            detectTapWithContext(
                onTap = { down, event ->
                    val scope = MouseClickScope(
                        down.buttons,
                        down.keyboardModifiers
                    )
                    val offset = event.mouseEvent?.let { IntOffset(it.xOnScreen, it.yOnScreen) }
                        ?: IntOffset.Zero

                    when {
                        rightClickIsContextMenu && event.mouseEvent?.isPopupTrigger == true -> onRightClickState.value(scope, offset)
                        down.buttons.isTertiaryPressed -> onMiddleClickState.value(scope, offset)
                        down.buttons.isSecondaryPressed -> onRightClickState.value(scope, offset)
                        else -> onClickState.value(scope, offset)
                    }
                }
            )
        }
    } else {
        Modifier
    }
    Modifier
        .genericClickableWithoutGesture(
            gestureModifiers = gesture,
            enabled = enabled,
            onClickLabel = onClickLabel,
            role = role,
            onMiddleClickLabel = onMiddleClickLabel,
            onRightClickLabel = onRightClickLabel,
            onMiddleClick = { onMiddleClick(EmptyClickContext, IntOffset.Zero) },
            onRightClick = { onRightClick(EmptyClickContext, IntOffset.Zero) },
            indication = null,
            interactionSource = remember { MutableInteractionSource() },
            onClick = { onClick(EmptyClickContext, IntOffset.Zero) }
        )
}

@Composable
@Suppress("ComposableModifierFactory")
private fun Modifier.genericClickableWithoutGesture(
    gestureModifiers: Modifier,
    interactionSource: MutableInteractionSource,
    indication: Indication?,
    enabled: Boolean = true,
    onClickLabel: String? = null,
    onMiddleClickLabel: String? = null,
    onMiddleClick: (() -> Unit),
    onRightClickLabel: String? = null,
    onRightClick: (() -> Unit),
    role: Role? = null,
    onClick: () -> Unit
): Modifier {
    val semanticModifier = Modifier.semantics(mergeDescendants = true) {
        if (role != null) {
            this.role = role
        }
        // b/156468846:  add long click semantics and double click if needed
        onClick(action = { onClick(); true }, label = onClickLabel)
        this[DesktopSemanticsActions.onMiddleClick] = AccessibilityAction(onMiddleClickLabel) { onMiddleClick(); true }
        this[DesktopSemanticsActions.onRightClick] = AccessibilityAction(onRightClickLabel) { onRightClick(); true }
        if (!enabled) {
            disabled()
        }
    }
    return this
        .then(semanticModifier)
        .indication(interactionSource, indication)
        .then(gestureModifiers)
}

object DesktopSemanticsActions {
    val onRightClick = ActionPropertyKey<() -> Boolean>("OnRightClick")
    val onMiddleClick = ActionPropertyKey<() -> Boolean>("OnMiddleClick")
}

private fun <T : Function<Boolean>> ActionPropertyKey(
    name: String
): SemanticsPropertyKey<AccessibilityAction<T>> {
    return SemanticsPropertyKey(
        name = name,
        mergePolicy = { parentValue, childValue ->
            AccessibilityAction(
                parentValue?.label ?: childValue.label,
                parentValue?.action ?: childValue.action
            )
        }
    )
}

@ExperimentalDesktopApi
internal val EmptyClickContext = MouseClickScope(
    PointerButtons(0), PointerKeyboardModifiers(0)
)

@ExperimentalDesktopApi
private suspend fun PointerInputScope.detectTapWithContext(
    onTap: ((PointerEvent, PointerEvent) -> Unit)? = null
) {
    forEachGesture {
        coroutineScope {
            awaitPointerEventScope {
                val down = awaitEventFirstDown().also {
                    it.changes.forEach { it.consumeDownChange() }
                }

                val up = waitForFirstInboundUp()
                if (up != null) {
                    up.changes.forEach { it.consumeDownChange() }
                    onTap?.invoke(down, up)
                }
            }
        }
    }
}

private suspend fun AwaitPointerEventScope.waitForFirstInboundUp(): PointerEvent? {
    while (true) {
        val event = awaitPointerEvent()
        val change = event.changes[0]
        if (change.changedToUp()) {
            return if (change.isOutOfBounds(size)) {
                null
            } else {
                event
            }
        }
    }
}
