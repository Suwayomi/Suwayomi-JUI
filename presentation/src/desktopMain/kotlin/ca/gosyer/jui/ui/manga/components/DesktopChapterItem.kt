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
import androidx.compose.ui.input.pointer.isCtrlPressed
import ca.gosyer.jui.i18n.MR
import ca.gosyer.jui.uicore.components.onRightClickContextMenu
import ca.gosyer.jui.uicore.resources.stringResource

actual fun Modifier.chapterItemModifier(
    onClick: () -> Unit,
    markRead: (() -> Unit)?,
    markUnread: (() -> Unit)?,
    bookmarkChapter: (() -> Unit)?,
    unBookmarkChapter: (() -> Unit)?,
    markPreviousAsRead: () -> Unit,
    onSelectChapter: (() -> Unit)?,
    onUnselectChapter: (() -> Unit)?
): Modifier = this
    .onClick(
        onClick = onClick,
        onLongClick = onSelectChapter
    )
    .onClick(
        onClick = onSelectChapter ?: onUnselectChapter ?: {},
        keyboardModifiers = { isCtrlPressed }
    )
    .onRightClickContextMenu(
        items = {
            getContextItems(
                markRead = markRead,
                markUnread = markUnread,
                bookmarkChapter = bookmarkChapter,
                unBookmarkChapter = unBookmarkChapter,
                markPreviousAsRead = markPreviousAsRead
            )
        }
    )

@Composable
@Stable
private fun getContextItems(
    markRead: (() -> Unit)?,
    markUnread: (() -> Unit)?,
    bookmarkChapter: (() -> Unit)?,
    unBookmarkChapter: (() -> Unit)?,
    markPreviousAsRead: () -> Unit
): List<ContextMenuItem> {
    return listOfNotNull(
        if (bookmarkChapter != null) ContextMenuItem(stringResource(MR.strings.action_bookmark), bookmarkChapter) else null,
        if (unBookmarkChapter != null) ContextMenuItem(stringResource(MR.strings.action_remove_bookmark), unBookmarkChapter) else null,
        if (markRead != null) ContextMenuItem(stringResource(MR.strings.action_mark_as_read), markRead) else null,
        if (markUnread != null) ContextMenuItem(stringResource(MR.strings.action_mark_as_unread), markUnread) else null,
        ContextMenuItem(stringResource(MR.strings.action_mark_previous_read), markPreviousAsRead),
    )
}
