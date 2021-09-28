/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.base.components

import androidx.compose.foundation.ContextMenuItem
import androidx.compose.foundation.ExperimentalDesktopApi
import androidx.compose.foundation.MouseClickScope
import androidx.compose.foundation.mouseClickable
import androidx.compose.material.CursorDropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.input.pointer.isPrimaryPressed
import androidx.compose.ui.input.pointer.isSecondaryPressed
import androidx.compose.ui.input.pointer.isTertiaryPressed
import androidx.compose.ui.platform.debugInspectorInfo
import androidx.compose.ui.semantics.AccessibilityAction
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsPropertyKey
import androidx.compose.ui.semantics.semantics

@OptIn(ExperimentalDesktopApi::class)
fun Modifier.contextMenuClickable(
    items: () -> List<ContextMenuItem>,
    onClickLabel: String? = null,
    onMiddleClickLabel: String? = null,
    onRightClickLabel: String? = null,
    onClick: MouseClickScope.() -> Unit = {},
    onMiddleClick: MouseClickScope.() -> Unit = {},
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
    enabled: Boolean = true,
    onClickLabel: String? = null,
    onMiddleClickLabel: String? = null,
    onRightClickLabel: String? = null,
    role: Role? = null,
    onClick: MouseClickScope.() -> Unit = {},
    onMiddleClick: MouseClickScope.() -> Unit = {},
    onRightClick: MouseClickScope.() -> Unit = {}
) = composed(
    inspectorInfo = debugInspectorInfo {
        name = "combinedMouseClickable"
        properties["enabled"] = enabled
        properties["onClickLabel"] = onClickLabel
        properties["onMiddleClickLabel"] = onMiddleClickLabel
        properties["onRightClickLabel"] = onRightClickLabel
        properties["role"] = role
        properties["onClick"] = onClick
        properties["onMiddleClick"] = onMiddleClick
        properties["onRightClick"] = onRightClick
    }
) {
    Modifier
        .mouseClickable(
            enabled,
            onClickLabel,
            role
        ) {
            when {
                buttons.isPrimaryPressed -> onClick()
                buttons.isSecondaryPressed -> onMiddleClick()
                buttons.isTertiaryPressed -> onRightClick()
            }
        }
        .semantics(mergeDescendants = true) {
            this[DesktopSemanticsActions.onMiddleClick] = AccessibilityAction(onMiddleClickLabel) { onMiddleClick(); true }
            this[DesktopSemanticsActions.onRightClick] = AccessibilityAction(onRightClickLabel) { onRightClick(); true }
        }
}

@OptIn(ExperimentalDesktopApi::class)
private object DesktopSemanticsActions {
    val onRightClick = ActionPropertyKey<MouseClickScope.() -> Boolean>("OnRightClick")
    val onMiddleClick = ActionPropertyKey<MouseClickScope.() -> Boolean>("OnMiddleClick")
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
