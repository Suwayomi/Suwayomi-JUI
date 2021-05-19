/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.base.components

import androidx.compose.foundation.Indication
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.forEachGesture
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.input.pointer.AwaitPointerEventScope
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.changedToDown
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.debugInspectorInfo
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.IntOffset
import java.awt.event.MouseEvent

private suspend fun AwaitPointerEventScope.awaitEventFirstDown(): PointerEvent {
    var event: PointerEvent
    do {
        event = awaitPointerEvent()
    } while (
        !event.changes.all { it.changedToDown() }
    )
    return event
}

fun Modifier.combinedMouseClickable(
    rightClickIsContextMenu: Boolean = true,
    onClick: (IntOffset) -> Unit = {},
    onMiddleClick: (IntOffset) -> Unit = {},
    onRightClick: (IntOffset) -> Unit = {}
) = composed(
    inspectorInfo = debugInspectorInfo {
        name = "combinedMouseClickable"
        properties["rightClickIsContextMenu"] = rightClickIsContextMenu
        properties["onClick"] = onClick
        properties["onMiddleClick"] = onMiddleClick
        properties["onRightClick"] = onRightClick
    }
) {
    Modifier.combinedMouseClickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = LocalIndication.current,
        rightClickIsContextMenu = rightClickIsContextMenu,
        onClick = onClick,
        onMiddleClick = onMiddleClick,
        onRightClick = onRightClick
    )
}

fun Modifier.combinedMouseClickable(
    interactionSource: MutableInteractionSource,
    indication: Indication?,
    enabled: Boolean = true,
    onClickLabel: String? = null,
    role: Role? = null,
    rightClickIsContextMenu: Boolean = true,
    onClick: (IntOffset) -> Unit,
    onMiddleClick: (IntOffset) -> Unit,
    onRightClick: (IntOffset) -> Unit
) = composed(
    inspectorInfo = debugInspectorInfo {
        name = "combinedMouseClickable"
        properties["enabled"] = enabled
        properties["onClickLabel"] = onClickLabel
        properties["role"] = role
        properties["rightClickIsContextMenu"] = rightClickIsContextMenu
        properties["onClick"] = onClick
        properties["onMiddleClick"] = onMiddleClick
        properties["onRightClick"] = onRightClick
        properties["indication"] = indication
        properties["interactionSource"] = interactionSource
    }
) {
    var lastEvent by remember { mutableStateOf<MouseEvent?>(null) }
    Modifier
        .clickable(interactionSource, indication, enabled, onClickLabel, role) {
            val savedLastEvent = lastEvent ?: return@clickable
            val offset = savedLastEvent.let { IntOffset(it.xOnScreen, it.yOnScreen) }
            when {
                rightClickIsContextMenu && savedLastEvent.isPopupTrigger -> onRightClick(offset)
                savedLastEvent.button == MouseEvent.BUTTON1 -> onClick(offset)
                savedLastEvent.button == MouseEvent.BUTTON2 -> onMiddleClick(offset)
                savedLastEvent.button == MouseEvent.BUTTON3 -> onRightClick(offset)
            }
        }
        .pointerInput(interactionSource) {
            forEachGesture {
                awaitPointerEventScope {
                    lastEvent = awaitEventFirstDown().mouseEvent
                }
            }
        }
}
