/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.manga.components

import androidx.compose.foundation.ContextMenuItem
import androidx.compose.foundation.onClick
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import ca.gosyer.jui.i18n.MR
import ca.gosyer.jui.uicore.components.onRightClickContextMenu
import ca.gosyer.jui.uicore.resources.stringResource

actual fun Modifier.chapterItemModifier(
    onClick: () -> Unit,
    toggleRead: () -> Unit,
    toggleBookmarked: () -> Unit,
    markPreviousAsRead: () -> Unit
): Modifier = this
    .onClick(
        onClick = onClick
    )
    .onRightClickContextMenu(
        items = {
            getContextItems(
                toggleRead,
                toggleBookmarked,
                markPreviousAsRead
            )
        }
    )

@Composable
@Stable
private fun getContextItems(
    toggleRead: () -> Unit,
    toggleBookmarked: () -> Unit,
    markPreviousAsRead: () -> Unit
): List<ContextMenuItem> {
    return listOf(
        ContextMenuItem(stringResource(MR.strings.action_toggle_read)) { toggleRead() },
        ContextMenuItem(stringResource(MR.strings.action_mark_previous_read)) { markPreviousAsRead() },
        ContextMenuItem(stringResource(MR.strings.action_toggle_bookmarked)) { toggleBookmarked() }
    )
}
