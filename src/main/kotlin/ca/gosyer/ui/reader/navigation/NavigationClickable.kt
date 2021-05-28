/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.reader.navigation

import androidx.compose.desktop.LocalAppWindow
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
import androidx.compose.ui.unit.IntSize
import ca.gosyer.ui.reader.model.Navigation
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

fun Modifier.navigationClickable(
    navigation: ViewerNavigation,
    onClick: (Navigation) -> Unit = {},
) = composed(
    inspectorInfo = debugInspectorInfo {
        name = "navigationClickable"
        properties["navigation"] = navigation
        properties["onClick"] = onClick
    }
) {
    Modifier.navigationClickable(
        navigation = navigation,
        interactionSource = remember { MutableInteractionSource() },
        onClick = onClick,
    )
}

fun Modifier.navigationClickable(
    navigation: ViewerNavigation,
    interactionSource: MutableInteractionSource,
    enabled: Boolean = true,
    onClickLabel: String? = null,
    role: Role? = null,
    onClick: (Navigation) -> Unit
) = composed(
    inspectorInfo = debugInspectorInfo {
        name = "navigationClickable"
        properties["navigation"] = navigation
        properties["enabled"] = enabled
        properties["onClickLabel"] = onClickLabel
        properties["role"] = role
        properties["onClick"] = onClick
        properties["interactionSource"] = interactionSource
    }
) {
    var lastEvent by remember { mutableStateOf<MouseEvent?>(null) }
    val window = LocalAppWindow.current
    Modifier
        .clickable(interactionSource, null, enabled, onClickLabel, role) {
            val savedLastEvent = lastEvent ?: return@clickable
            val offset = savedLastEvent.let { IntOffset(it.x, it.y) }
            onClick(navigation.getAction(offset, IntSize(window.width, window.height)))
        }
        .pointerInput(interactionSource) {
            forEachGesture {
                awaitPointerEventScope {
                    lastEvent = awaitEventFirstDown().mouseEvent
                }
            }
        }
}
