/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.categories.components

import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import ca.gosyer.i18n.MR
import ca.gosyer.ui.base.dialog.getMaterialDialogProperties
import ca.gosyer.ui.categories.CategoriesScreenViewModel
import ca.gosyer.uicore.components.keyboardHandler
import ca.gosyer.uicore.resources.stringResource
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.MaterialDialogState
import com.vanpra.composematerialdialogs.message
import com.vanpra.composematerialdialogs.title

@Composable
fun RenameDialog(
    state: MaterialDialogState,
    category: CategoriesScreenViewModel.MenuCategory,
    onRename: (String) -> Unit
) {
    var newName by remember { mutableStateOf(TextFieldValue(category.name)) }

    MaterialDialog(
        state,
        buttons = {
            positiveButton(stringResource(MR.strings.action_rename)) {
                if (newName.text != category.name) {
                    onRename(newName.text)
                }
            }
            negativeButton(stringResource(MR.strings.action_cancel))
        },
        properties = getMaterialDialogProperties(),
    ) {
        title("Rename Category")
        TextField(
            newName,
            onValueChange = {
                newName = it
            },
            modifier = Modifier.keyboardHandler(singleLine = true)
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
        properties = getMaterialDialogProperties(),
    ) {
        title("Delete Category")
        message(stringResource(MR.strings.categories_delete_confirm, category.name))
    }
}

@Composable
fun CreateDialog(
    state: MaterialDialogState,
    onCreate: (String) -> Unit
) {
    var name by remember { mutableStateOf(TextFieldValue("")) }

    MaterialDialog(
        state,
        buttons = {
            positiveButton(stringResource(MR.strings.action_create)) {
                onCreate(name.text)
            }
            negativeButton(stringResource(MR.strings.action_cancel))
        },
        properties = getMaterialDialogProperties(),
    ) {
        title("Create Category")
        TextField(
            name,
            onValueChange = {
                name = it
            },
            singleLine = true,
            modifier = Modifier.keyboardHandler(singleLine = true)
        )
    }
}
