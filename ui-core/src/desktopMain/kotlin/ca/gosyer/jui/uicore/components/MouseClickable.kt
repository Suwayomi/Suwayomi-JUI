/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.uicore.components

import androidx.compose.foundation.ContextMenuItem
import androidx.compose.foundation.PointerMatcher
import androidx.compose.foundation.onClick
import androidx.compose.material.CursorDropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.input.pointer.PointerButton

fun Modifier.onRightClickContextMenu(
    items: @Composable () -> List<ContextMenuItem>,
    enabled: Boolean = true,
) = composed {
    var expanded by remember { mutableStateOf(false) }
    CursorDropdownMenu(
        expanded,
        onDismissRequest = { expanded = false },
    ) {
        items().forEach { item ->
            DropdownMenuItem(
                onClick = {
                    expanded = false
                    item.onClick()
                },
            ) {
                Text(text = item.label)
            }
        }
    }
    Modifier.onClick(
        enabled = enabled,
        matcher = PointerMatcher.mouse(PointerButton.Secondary),
        onClick = { expanded = true },
    )
}
