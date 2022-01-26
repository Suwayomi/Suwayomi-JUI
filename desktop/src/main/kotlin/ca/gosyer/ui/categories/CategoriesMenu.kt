/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.categories

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.Card
import androidx.compose.material.ContentAlpha
import androidx.compose.material.ExtendedFloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.List
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ca.gosyer.build.BuildConfig
import ca.gosyer.ui.base.resources.stringResource
import ca.gosyer.ui.base.vm.viewModel
import ca.gosyer.util.compose.ThemedWindow
import ca.gosyer.util.lang.launchApplication
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import mu.KotlinLogging

@OptIn(DelicateCoroutinesApi::class)
fun openCategoriesMenu(notifyFinished: (() -> Unit)? = null) {
    launchApplication {
        ThemedWindow(
            ::exitApplication,
            title = "${BuildConfig.NAME} - Categories"
        ) {
            CategoriesMenu(notifyFinished)
        }
    }
}

@OptIn(DelicateCoroutinesApi::class)
@Composable
fun CategoriesMenu(notifyFinished: (() -> Unit)? = null) {
    val vm = viewModel<CategoriesMenuViewModel>()
    val categories by vm.categories.collectAsState()
    DisposableEffect(Unit) {
        onDispose {
            val logger = KotlinLogging.logger {}
            val handler = CoroutineExceptionHandler { _, throwable ->
                logger.debug { throwable }
            }
            GlobalScope.launch(handler) {
                vm.updateRemoteCategories()
                notifyFinished?.invoke()
            }
        }
    }

    Surface {
        Box {
            val state = rememberLazyListState()
            LazyColumn(modifier = Modifier.fillMaxSize(), state = state,) {
                itemsIndexed(categories) { i, category ->
                    CategoryRow(
                        category = category,
                        moveUpEnabled = i != 0,
                        moveDownEnabled = i != categories.lastIndex,
                        onMoveUp = { vm.moveUp(category) },
                        onMoveDown = { vm.moveDown(category) },
                        onRename = {
                            openRenameDialog(category) {
                                vm.renameCategory(category, it)
                            }
                        },
                        onDelete = {
                            openDeleteDialog(category) {
                                vm.deleteCategory(category)
                            }
                        },
                    )
                }
                item {
                    Spacer(Modifier.height(80.dp).fillMaxWidth())
                }
            }
            ExtendedFloatingActionButton(
                text = { Text(text = stringResource("action_add")) },
                icon = { Icon(imageVector = Icons.Rounded.Add, contentDescription = null) },
                modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
                onClick = {
                    openCreateDialog {
                        vm.createCategory(it)
                    }
                }
            )
            VerticalScrollbar(
                rememberScrollbarAdapter(state),
                Modifier.align(Alignment.CenterEnd)
                    .fillMaxHeight()
                    .padding(horizontal = 4.dp, vertical = 8.dp)
            )
        }
    }
}

@Composable
private fun CategoryRow(
    category: CategoriesMenuViewModel.MenuCategory,
    moveUpEnabled: Boolean = true,
    moveDownEnabled: Boolean = true,
    onMoveUp: () -> Unit = {},
    onMoveDown: () -> Unit = {},
    onRename: () -> Unit = {},
    onDelete: () -> Unit = {},
) {
    Card(Modifier.padding(8.dp)) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.List,
                    modifier = Modifier.padding(16.dp),
                    tint = MaterialTheme.colors.primary,
                    contentDescription = null,
                )
                Text(
                    text = category.name,
                    modifier = Modifier.weight(1f).padding(end = 16.dp)
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                    val enabledColor = LocalContentColor.current
                    val disabledColor = enabledColor.copy(ContentAlpha.disabled)
                    IconButton(
                        onClick = onMoveUp,
                        enabled = moveUpEnabled
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.KeyboardArrowUp,
                            tint = if (moveUpEnabled) enabledColor else disabledColor,
                            contentDescription = null
                        )
                    }
                    IconButton(
                        onClick = onMoveDown,
                        enabled = moveDownEnabled
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.KeyboardArrowDown,
                            tint = if (moveDownEnabled) enabledColor else disabledColor,
                            contentDescription = null
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(onClick = onRename) {
                        Icon(
                            imageVector = Icons.Rounded.Edit,
                            contentDescription = null
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Rounded.Delete,
                            contentDescription = null
                        )
                    }
                }
            }
        }
    }
}
