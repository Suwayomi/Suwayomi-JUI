/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.downloads

import ca.gosyer.data.base.WebsocketService
import ca.gosyer.data.download.DownloadService
import ca.gosyer.uicore.vm.ContextWrapper

internal actual fun startDownloadService(
    contextWrapper: ContextWrapper,
    downloadService: DownloadService,
    actions: WebsocketService.Actions
) {
    downloadService.init()
}