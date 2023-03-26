/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.main.components

import ca.gosyer.jui.domain.base.WebsocketService
import ca.gosyer.jui.domain.library.service.LibraryUpdateService
import ca.gosyer.jui.uicore.vm.ContextWrapper

internal actual fun startLibraryUpdatesService(
    contextWrapper: ContextWrapper,
    libraryUpdatesService: LibraryUpdateService,
    actions: WebsocketService.Actions,
) {
    libraryUpdatesService.init()
}
