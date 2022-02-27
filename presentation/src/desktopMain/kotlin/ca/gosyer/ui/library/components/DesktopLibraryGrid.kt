/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.library.components

import androidx.compose.foundation.ContextMenuItem
import androidx.compose.ui.Modifier
import ca.gosyer.uicore.components.contextMenuClickable

actual fun Modifier.libraryMangaModifier(
    onClickManga: () -> Unit,
    onClickRemoveManga: () -> Unit
): Modifier = Modifier.contextMenuClickable(
    {
        listOf(
            ContextMenuItem("Unfavorite", onClickRemoveManga)
        )
    },
    onClick = { onClickManga() }
)