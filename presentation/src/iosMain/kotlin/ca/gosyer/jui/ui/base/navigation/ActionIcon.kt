/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.base.navigation

import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

// todo
@Composable
actual fun ActionIcon(onClick: () -> Unit, contentDescription: String, icon: ImageVector) {
    IconButton(onClick = onClick) {
        Icon(icon, contentDescription)
    }
}
