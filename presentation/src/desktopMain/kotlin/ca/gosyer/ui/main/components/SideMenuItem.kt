/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.main.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import ca.gosyer.ui.main.Menu
import ca.gosyer.uicore.resources.stringResource
import cafe.adriel.voyager.core.screen.Screen

@Composable
fun SideMenuItem(selected: Boolean, topLevelMenu: Menu, newRoot: (Screen) -> Unit) {
    SideMenuItem(
        selected,
        stringResource(topLevelMenu.textKey),
        topLevelMenu.createScreen,
        topLevelMenu.selectedIcon,
        topLevelMenu.unselectedIcon,
        topLevelMenu.extraInfo,
        newRoot
    )
}

@Composable
private fun SideMenuItem(
    selected: Boolean,
    text: String,
    createScreen: () -> Screen,
    selectedIcon: ImageVector,
    unselectedIcon: ImageVector,
    extraInfo: (@Composable () -> Unit)? = null,
    onClick: (Screen) -> Unit
) {
    Card(
        Modifier.fillMaxWidth(),
        backgroundColor = if (!selected) {
            Color.Transparent
        } else {
            MaterialTheme.colors.primary.copy(0.30F)
        },
        elevation = 0.dp,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
                .defaultMinSize(minHeight = 40.dp)
                .clickable(
                    onClick = { onClick(createScreen()) },
                    // onMiddleClick = { onMiddleClick?.invoke() } todo
                )
        ) {
            Spacer(Modifier.width(16.dp))
            Icon(
                imageVector = if (selected) {
                    selectedIcon
                } else {
                    unselectedIcon
                },
                contentDescription = text,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colors.onSurface
            )
            Spacer(Modifier.width(16.dp))
            Column {
                Text(text, color = MaterialTheme.colors.onSurface)
                if (extraInfo != null) {
                    extraInfo()
                }
            }
        }
    }
}
