/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.base.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.size
import androidx.compose.material.DropdownMenu
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp

@Composable
fun DropdownIconButton(
    key: Any? = Unit,
    dropdownItems: @Composable ColumnScope.() -> Unit,
    content: @Composable BoxScope.() -> Unit
) {
    var showMenu by remember(key) { mutableStateOf(false) }
    var offset by remember(key) { mutableStateOf(DpOffset(0.dp, 0.dp)) }
    DropdownMenu(
        expanded = showMenu,
        onDismissRequest = { showMenu = false },
        offset = offset,
        content = dropdownItems
    )
    Box(
        modifier = Modifier.size(48.dp)
            .clickable(
                remember { MutableInteractionSource() },
                role = Role.Button,
                indication = rememberRipple(bounded = false, radius = 24.dp)
            ) {
                showMenu = true
            }
            .onGloballyPositioned {
                offset = it.positionInWindow().let { DpOffset(it.x.dp, it.y.dp) }
            },
        contentAlignment = Alignment.Center,
        content = content
    )
}
