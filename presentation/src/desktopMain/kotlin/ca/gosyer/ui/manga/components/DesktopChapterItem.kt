/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.manga.components

import androidx.compose.foundation.ContextMenuItem
import androidx.compose.ui.Modifier
import ca.gosyer.uicore.components.contextMenuClickable

actual fun Modifier.chapterItemModifier(
    onClick: () -> Unit,
    toggleRead: () -> Unit,
    toggleBookmarked: () -> Unit,
    markPreviousAsRead: () -> Unit
): Modifier = Modifier.contextMenuClickable(
    {
        listOf(
            ContextMenuItem("Toggle read") { toggleRead() },
            ContextMenuItem("Mark previous as read") { markPreviousAsRead() },
            ContextMenuItem("Toggle bookmarked") { toggleBookmarked() }
        )
    },
    onClick = { onClick() }
)