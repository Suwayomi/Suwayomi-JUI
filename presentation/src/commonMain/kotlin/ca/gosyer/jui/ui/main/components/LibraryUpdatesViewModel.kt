/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.main.components

import ca.gosyer.jui.domain.base.WebsocketService.Actions
import ca.gosyer.jui.domain.library.service.LibraryUpdateService
import ca.gosyer.jui.uicore.vm.ContextWrapper
import ca.gosyer.jui.uicore.vm.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.asStateFlow
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject
import org.lighthousegames.logging.logging

class LibraryUpdatesViewModel @Inject constructor(
    private val libraryUpdateService: LibraryUpdateService,
    private val contextWrapper: ContextWrapper,
    @Assisted standalone: Boolean,
) : ViewModel(contextWrapper) {
    private val uiScope = if (standalone) {
        MainScope()
    } else {
        null
    }

    override val scope: CoroutineScope
        get() = uiScope ?: super.scope

    val serviceStatus = LibraryUpdateService.status.asStateFlow()
    val updateStatus = LibraryUpdateService.updateStatus.asStateFlow()

    fun restartLibraryUpdates() = startLibraryUpdatesService(contextWrapper, libraryUpdateService, Actions.RESTART)

    override fun onDispose() {
        super.onDispose()
        uiScope?.cancel()
    }

    private companion object {
        private val log = logging()
    }
}
