/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.main.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import ca.gosyer.jui.domain.base.WebsocketService
import ca.gosyer.jui.domain.library.model.JobStatus
import ca.gosyer.jui.i18n.MR
import ca.gosyer.jui.ui.base.LocalViewModels
import ca.gosyer.jui.uicore.resources.stringResource

@Composable
fun LibraryUpdatesExtraInfo() {
    val viewModels = LocalViewModels.current
    val vm = remember { viewModels.libraryUpdatesViewModel(true) }
    DisposableEffect(vm) {
        onDispose(vm::onDispose)
    }
    val serviceStatus by vm.serviceStatus.collectAsState()
    val updateStatus by vm.updateStatus.collectAsState()

    fun Map<JobStatus, List<*>>.getSize(jobStatus: JobStatus): Int = get(jobStatus)?.size ?: 0
    val current = remember(updateStatus) {
        updateStatus.statusMap.run {
            getSize(JobStatus.COMPLETE) + getSize(JobStatus.FAILED)
        }
    }
    val total = remember(updateStatus) {
        updateStatus.statusMap.run {
            getSize(JobStatus.COMPLETE) + getSize(JobStatus.FAILED) + getSize(JobStatus.PENDING) + getSize(JobStatus.RUNNING)
        }
    }

    val text = when (serviceStatus) {
        WebsocketService.Status.STARTING -> stringResource(MR.strings.downloads_loading)
        WebsocketService.Status.RUNNING -> {
            if (updateStatus.running) {
                stringResource(MR.strings.notification_updating, current, total)
            } else {
                null
            }
        }
        WebsocketService.Status.STOPPED -> null
    }
    if (!text.isNullOrBlank()) {
        Text(
            text,
            style = MaterialTheme.typography.body2,
            color = LocalContentColor.current.copy(alpha = ContentAlpha.disabled)
        )
    } else if (serviceStatus == WebsocketService.Status.STOPPED) {
        Box(
            Modifier.fillMaxWidth()
                .clip(MaterialTheme.shapes.medium)
                .clickable(onClick = vm::restartLibraryUpdates)
        ) {
            Text(
                stringResource(MR.strings.downloads_stopped),
                style = MaterialTheme.typography.body2,
                color = Color.Red.copy(alpha = ContentAlpha.disabled)
            )
        }
    }
}
