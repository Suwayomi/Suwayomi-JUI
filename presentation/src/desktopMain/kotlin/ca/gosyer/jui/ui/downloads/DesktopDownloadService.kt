/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.downloads

import ca.gosyer.jui.domain.base.WebsocketService
import ca.gosyer.jui.domain.download.service.DownloadService
import ca.gosyer.jui.uicore.vm.ContextWrapper
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.launchIn

internal actual fun startDownloadService(
    contextWrapper: ContextWrapper,
    downloadService: DownloadService,
    actions: WebsocketService.Actions,
) {
    downloadService.getSubscription().launchIn(GlobalScope)
}
