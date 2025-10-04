/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.main.components

import ca.gosyer.jui.domain.updates.interactor.UpdateChecker
import ca.gosyer.jui.uicore.vm.ContextWrapper
import ca.gosyer.jui.uicore.vm.ViewModel
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.shareIn
import me.tatarka.inject.annotations.Inject
import org.lighthousegames.logging.logging

@Inject
class TrayViewModel
constructor(
    updateChecker: UpdateChecker,
    contextWrapper: ContextWrapper,
) : ViewModel(contextWrapper) {
    override val scope = MainScope()

    val updateFound = updateChecker
        .asFlow(false)
        .catch { log.warn(it) { "Failed to check for updates" } }
        .filterIsInstance<UpdateChecker.Update.UpdateFound>()
        .shareIn(scope, SharingStarted.Eagerly, 1)

    override fun onDispose() {
        super.onDispose()
        scope.cancel()
    }

    companion object {
        private val log = logging()
    }
}
