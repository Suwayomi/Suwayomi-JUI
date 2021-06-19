/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.categories

import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.text.input.TextFieldValue
import ca.gosyer.ui.base.WindowDialog
import ca.gosyer.ui.base.resources.stringResource
import kotlinx.coroutines.flow.MutableStateFlow

fun openRenameDialog(
    category: CategoriesMenuViewModel.MenuCategory,
    onRename: (String) -> Unit
) {
    val newName = MutableStateFlow(TextFieldValue(category.name))

    WindowDialog(
        title = "TachideskJUI - Categories - Rename Dialog",
        positiveButtonText = "Rename",
        onPositiveButton = {
            if (newName.value.text != category.name) {
                onRename(newName.value.text)
            }
        }
    ) {
        val newNameState by newName.collectAsState()

        TextField(
            newNameState,
            onValueChange = {
                newName.value = it
            }
        )
    }
}

fun openDeleteDialog(
    category: CategoriesMenuViewModel.MenuCategory,
    onDelete: (CategoriesMenuViewModel.MenuCategory) -> Unit
) {
    WindowDialog(
        title = "TachideskJUI - Categories - Delete Dialog",
        positiveButtonText = "Yes",
        onPositiveButton = {
            onDelete(category)
        },
        negativeButtonText = "No"
    ) {
        Text(stringResource("categories_delete_confirm", category.name))
    }
}

fun openCreateDialog(
    onCreate: (String) -> Unit
) {
    val name = MutableStateFlow(TextFieldValue(""))

    WindowDialog(
        title = "TachideskJUI - Categories - Create Dialog",
        positiveButtonText = "Create",
        onPositiveButton = {
            onCreate(name.value.text)
        }
    ) {
        val nameState by name.collectAsState()

        TextField(
            nameState,
            onValueChange = {
                name.value = it
            }
        )
    }
}
