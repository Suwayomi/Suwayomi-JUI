/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.library.components

import androidx.compose.foundation.ContextMenuItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import ca.gosyer.i18n.MR
import ca.gosyer.uicore.components.contextMenuClickable
import ca.gosyer.uicore.resources.stringResource

actual fun Modifier.libraryMangaModifier(
    onClickManga: () -> Unit,
    onClickRemoveManga: () -> Unit
): Modifier = Modifier.contextMenuClickable(
    {
        getContextItems(onClickRemoveManga)
    },
    onClick = { onClickManga() }
)

@Composable
@Stable
private fun getContextItems(
    onClickRemoveManga: () -> Unit
): List<ContextMenuItem> {
    return listOf(
        ContextMenuItem(stringResource(MR.strings.action_remove_favorite)) { onClickRemoveManga() },
    )
}