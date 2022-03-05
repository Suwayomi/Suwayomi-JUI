/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.manga.components

import androidx.compose.foundation.ContextMenuItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import ca.gosyer.i18n.MR
import ca.gosyer.uicore.components.contextMenuClickable
import ca.gosyer.uicore.resources.stringResource

actual fun Modifier.chapterItemModifier(
    onClick: () -> Unit,
    toggleRead: () -> Unit,
    toggleBookmarked: () -> Unit,
    markPreviousAsRead: () -> Unit
): Modifier = Modifier.contextMenuClickable(
    {
        getContextItems(
            toggleRead,
            toggleBookmarked,
            markPreviousAsRead
        )
    },
    onClick = { onClick() }
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
