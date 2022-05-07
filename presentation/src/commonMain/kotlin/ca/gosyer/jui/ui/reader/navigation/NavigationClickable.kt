/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.reader.navigation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.forEachGesture
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.debugInspectorInfo
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.toSize
import ca.gosyer.jui.ui.reader.model.Navigation
import ca.gosyer.jui.ui.util.compose.contains
import kotlinx.coroutines.flow.MutableStateFlow

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
    navigationClickable(
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
): Modifier = composed(
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
    val offsetEvent = remember { MutableStateFlow<Offset?>(null) }
    val layoutSize = remember { MutableStateFlow(Size.Zero) }
    this
        .clickable(interactionSource, null, enabled, onClickLabel, role) {
            val offset = offsetEvent.value ?: return@clickable
            val size = layoutSize.value
            if (offset in size) {
                onClick(navigation.getAction(offset, size))
            }
        }
        .pointerInput(interactionSource) {
            forEachGesture {
                awaitPointerEventScope {
                    offsetEvent.value = awaitFirstDown().position
                }
            }
        }
        .onGloballyPositioned {
            layoutSize.value = it.size.toSize()
        }
}
