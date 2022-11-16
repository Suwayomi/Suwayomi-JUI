/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.updates.components

import androidx.compose.foundation.combinedClickable
import androidx.compose.ui.Modifier

actual fun Modifier.updatesItemModifier(
    onClick: () -> Unit,
    markRead: (() -> Unit)?,
    markUnread: (() -> Unit)?,
    bookmarkChapter: (() -> Unit)?,
    unBookmarkChapter: (() -> Unit)?,
    onSelectChapter: (() -> Unit)?,
    onUnselectChapter: (() -> Unit)?
): Modifier = combinedClickable(
    onClick = onUnselectChapter ?: onClick,
    onLongClick = onSelectChapter
)
