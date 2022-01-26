/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.main.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import ca.gosyer.ui.base.components.combinedMouseClickable
import dev.icerock.moko.resources.compose.stringResource
import ca.gosyer.i18n.MR
import ca.gosyer.ui.main.Routes
import ca.gosyer.ui.main.TopLevelMenus

@Composable
fun SideMenuItem(selected: Boolean, topLevelMenu: TopLevelMenus, newRoot: (Routes) -> Unit) {
    SideMenuItem(
        selected,
        stringResource(topLevelMenu.textKey),
        topLevelMenu.menu,
        topLevelMenu.selectedIcon,
        topLevelMenu.unselectedIcon,
        topLevelMenu.openInNewWindow,
        topLevelMenu.extraInfo,
        newRoot
    )
}

@Composable
private fun SideMenuItem(
    selected: Boolean,
    text: String,
    menu: Routes,
    selectedIcon: ImageVector,
    unselectedIcon: ImageVector,
    onMiddleClick: () -> Unit,
    extraInfo: (@Composable () -> Unit)? = null,
    onClick: (Routes) -> Unit
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
                .height(40.dp)
                .combinedMouseClickable(
                    onClick = { onClick(menu) },
                    onMiddleClick = { onMiddleClick() }
                )
        ) {
            Spacer(Modifier.width(16.dp))
            Image(
                if (selected) {
                    selectedIcon
                } else {
                    unselectedIcon
                },
                text,
                Modifier.size(20.dp),
                colorFilter = ColorFilter.tint(MaterialTheme.colors.onSurface)
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
