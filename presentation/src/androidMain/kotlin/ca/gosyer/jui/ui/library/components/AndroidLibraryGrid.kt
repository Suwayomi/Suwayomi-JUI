/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.library.components

import androidx.compose.foundation.combinedClickable
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import ca.gosyer.jui.i18n.MR
import ca.gosyer.jui.uicore.resources.stringResource

actual fun Modifier.libraryMangaModifier(
    onClickManga: () -> Unit,
    onClickRemoveManga: () -> Unit
): Modifier = composed {
    var expanded by remember { mutableStateOf(false) }
    DropdownMenu(
        expanded,
        onDismissRequest = { expanded = false }
    ) {
        listOf(
            stringResource(MR.strings.action_remove_favorite) to onClickRemoveManga
        ).forEach { (label, onClick) ->
            DropdownMenuItem(
                onClick = {
                    expanded = false
                    onClick()
                }
            ) {
                Text(text = label)
            }
        }
    }

    Modifier.combinedClickable(
        onClick = { onClickManga() },
        onLongClick = {
            expanded = true
        }
    )
}
