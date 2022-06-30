/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.downloads

import android.content.Intent
import androidx.core.content.ContextCompat
import ca.gosyer.jui.domain.base.WebsocketService
import ca.gosyer.jui.domain.download.service.DownloadService
import ca.gosyer.jui.uicore.vm.ContextWrapper

internal actual fun startDownloadService(
    contextWrapper: ContextWrapper,
    downloadService: DownloadService,
    actions: WebsocketService.Actions
) {
    val intent = Intent(
        contextWrapper,
        Class.forName("ca.gosyer.jui.android.data.download.AndroidDownloadService")
    ).apply {
        action = actions.name
    }
    ContextCompat.startForegroundService(contextWrapper, intent)
}
