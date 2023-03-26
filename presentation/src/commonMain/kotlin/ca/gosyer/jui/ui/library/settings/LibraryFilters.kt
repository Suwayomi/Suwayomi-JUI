/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.library.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.TriStateCheckbox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.state.ToggleableState
import ca.gosyer.jui.domain.library.model.FilterState
import ca.gosyer.jui.i18n.MR
import ca.gosyer.jui.ui.sources.browse.filter.SourceFilterAction
import ca.gosyer.jui.uicore.resources.stringResource

@Composable
fun getLibraryFilters(vm: LibrarySettingsViewModel): @Composable () -> Unit = remember(vm) {
    @Composable {
        LibraryFilters(
            downloaded = vm.filterDownloaded.collectAsState().value,
            unread = vm.filterUnread.collectAsState().value,
            completed = vm.filterCompleted.collectAsState().value,
            setDownloadedFilter = { vm.filterDownloaded.value = it },
            setUnreadFilter = { vm.filterUnread.value = it },
            setCompletedFilter = { vm.filterCompleted.value = it },
        )
    }
}

@Composable
fun LibraryFilters(
    downloaded: FilterState,
    unread: FilterState,
    completed: FilterState,
    setDownloadedFilter: (FilterState) -> Unit,
    setUnreadFilter: (FilterState) -> Unit,
    setCompletedFilter: (FilterState) -> Unit,
) {
    Column(Modifier.fillMaxWidth()) {
        Filter(
            stringResource(MR.strings.filter_downloaded),
            downloaded,
            onClick = { setDownloadedFilter(toggleState(downloaded)) },
        )
        Filter(
            stringResource(MR.strings.filter_unread),
            unread,
            onClick = { setUnreadFilter(toggleState(unread)) },
        )
        Filter(
            stringResource(MR.strings.filter_completed),
            completed,
            onClick = { setCompletedFilter(toggleState(completed)) },
        )
    }
}

fun toggleState(filterState: FilterState) = when (filterState) {
    FilterState.IGNORED -> FilterState.INCLUDED
    FilterState.INCLUDED -> FilterState.EXCLUDED
    FilterState.EXCLUDED -> FilterState.IGNORED
}

@Composable
private fun Filter(text: String, state: FilterState, onClick: () -> Unit) {
    SourceFilterAction(
        text,
        onClick = onClick,
        action = {
            TriStateCheckbox(
                state = when (state) {
                    FilterState.INCLUDED -> ToggleableState.On
                    FilterState.EXCLUDED -> ToggleableState.Indeterminate
                    FilterState.IGNORED -> ToggleableState.Off
                },
                onClick = null,
            )
        },
    )
}
