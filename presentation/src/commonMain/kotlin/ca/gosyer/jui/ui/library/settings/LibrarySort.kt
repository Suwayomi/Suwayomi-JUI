/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.library.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import ca.gosyer.jui.data.library.model.Sort
import ca.gosyer.jui.ui.sources.browse.filter.SourceFilterAction
import ca.gosyer.jui.uicore.resources.stringResource

@Composable
fun getLibrarySort(vm: LibrarySettingsViewModel): @Composable () -> Unit = remember(vm) {
    @Composable {
        LibrarySort(
            mode = vm.sortMode.collectAsState().value,
            ascending = vm.sortAscending.collectAsState().value,
            setMode = {
                vm.sortMode.value = it
                vm.sortAscending.value = true
            },
            setAscending = { vm.sortAscending.value = it }
        )
    }
}

@Composable
fun LibrarySort(
    mode: Sort,
    ascending: Boolean,
    setMode: (Sort) -> Unit,
    setAscending: (Boolean) -> Unit
) {
    Column(Modifier.fillMaxWidth()) {
        Sort.values().asList().fastForEach { sort ->
            SourceFilterAction(
                name = stringResource(sort.res),
                onClick = {
                    if (mode == sort) {
                        setAscending(!ascending)
                    } else {
                        setMode(sort)
                    }
                },
                action = {
                    if (mode == sort) {
                        Icon(
                            imageVector = when (ascending) {
                                true -> Icons.Rounded.ArrowUpward
                                false -> Icons.Rounded.ArrowDownward
                            },
                            contentDescription = stringResource(sort.res),
                            modifier = Modifier.fillMaxHeight()
                        )
                    } else {
                        Box(Modifier.size(24.dp))
                    }
                }
            )
        }
    }
}
