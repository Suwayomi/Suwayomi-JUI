/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.main.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ca.gosyer.data.download.DownloadService
import ca.gosyer.ui.base.resources.stringResource
import ca.gosyer.ui.base.vm.viewModel
import ca.gosyer.ui.downloads.DownloadsMenuViewModel

@Composable
fun DownloadsExtraInfo() {
    val vm = viewModel<DownloadsMenuViewModel>()
    val status by vm.serviceStatus.collectAsState()
    val list by vm.downloadQueue.collectAsState()
    val text = when (status) {
        DownloadService.Status.STARTING -> stringResource("downloads_loading")
        DownloadService.Status.RUNNING -> {
            if (list.isNotEmpty()) {
                stringResource("downloads_remaining", list.size)
            } else null
        }
        DownloadService.Status.STOPPED -> null
    }
    if (text != null) {
        Text(
            text,
            style = MaterialTheme.typography.body2,
            color = LocalContentColor.current.copy(alpha = ContentAlpha.disabled)
        )
    } else if (status == DownloadService.Status.STOPPED) {
        Surface(onClick = vm::restartDownloader, shape = RoundedCornerShape(4.dp)) {
            Text(
                stringResource("downloads_stopped"),
                style = MaterialTheme.typography.body2,
                color = Color.Red.copy(alpha = ContentAlpha.disabled)
            )
        }
    }
}
