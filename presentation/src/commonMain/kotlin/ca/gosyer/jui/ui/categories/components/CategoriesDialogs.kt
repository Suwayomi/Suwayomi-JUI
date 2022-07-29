/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.categories.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import ca.gosyer.jui.i18n.MR
import ca.gosyer.jui.ui.base.dialog.getMaterialDialogProperties
import ca.gosyer.jui.ui.categories.CategoriesScreenViewModel
import ca.gosyer.jui.uicore.components.keyboardHandler
import ca.gosyer.jui.uicore.resources.stringResource
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.MaterialDialogState
import com.vanpra.composematerialdialogs.TextFieldStyle
import com.vanpra.composematerialdialogs.input
import com.vanpra.composematerialdialogs.message
import com.vanpra.composematerialdialogs.title

@Composable
fun RenameDialog(
    state: MaterialDialogState,
    category: CategoriesScreenViewModel.MenuCategory,
    onRename: (String) -> Unit
) {
    MaterialDialog(
        state,
        buttons = {
            positiveButton(stringResource(MR.strings.action_rename))
            negativeButton(stringResource(MR.strings.action_cancel))
        },
        properties = getMaterialDialogProperties()
    ) {
        title(stringResource(MR.strings.categories_rename))
        input(
            label = "",
            prefill = category.name,
            textFieldStyle = TextFieldStyle.Outlined,
            onInput = { onRename(it) },
            maxLines = 1,
            singleLine = true,
            modifier = Modifier.keyboardHandler(true, enterAction = { it.moveFocus(FocusDirection.Next) })
        )
    }
}

@Composable
fun DeleteDialog(
    state: MaterialDialogState,
    category: CategoriesScreenViewModel.MenuCategory,
    onDelete: (CategoriesScreenViewModel.MenuCategory) -> Unit
) {
    MaterialDialog(
        state,
        buttons = {
            positiveButton(stringResource(MR.strings.action_yes)) {
                onDelete(category)
            }
            negativeButton(stringResource(MR.strings.action_no))
        },
        properties = getMaterialDialogProperties()
    ) {
        title(stringResource(MR.strings.categories_delete))
        message(stringResource(MR.strings.categories_delete_confirm, category.name))
    }
}

@Composable
fun CreateDialog(
    state: MaterialDialogState,
    onCreate: (String) -> Unit
) {
    MaterialDialog(
        state,
        buttons = {
            positiveButton(stringResource(MR.strings.action_create))
            negativeButton(stringResource(MR.strings.action_cancel))
        },
        properties = getMaterialDialogProperties()
    ) {
        title(stringResource(MR.strings.categories_create))
        input(
            label = "",
            textFieldStyle = TextFieldStyle.Outlined,
            onInput = { onCreate(it) },
            maxLines = 1,
            singleLine = true,
            modifier = Modifier.keyboardHandler(true, enterAction = { it.moveFocus(FocusDirection.Next) })
        )
    }
}
