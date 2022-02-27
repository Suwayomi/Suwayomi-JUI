/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.manga.components

import androidx.compose.foundation.clickable
import androidx.compose.ui.Modifier

actual fun Modifier.chapterItemModifier(
    onClick: () -> Unit,
    toggleRead: () -> Unit,
    toggleBookmarked: () -> Unit,
    markPreviousAsRead: () -> Unit
): Modifier = Modifier.clickable(
    onClick = { onClick() }
)