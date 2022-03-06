/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.main.components

import ca.gosyer.data.update.UpdateChecker
import ca.gosyer.data.update.UpdatePreferences
import ca.gosyer.data.update.model.GithubRelease
import ca.gosyer.uicore.vm.ContextWrapper
import ca.gosyer.uicore.vm.ViewModel
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import me.tatarka.inject.annotations.Inject

class TrayViewModel @Inject constructor(
    updateChecker: UpdateChecker,
    updatePreferences: UpdatePreferences,
    contextWrapper: ContextWrapper
) : ViewModel(contextWrapper) {
    override val scope = MainScope()

    private val _updateFound = MutableSharedFlow<GithubRelease>()
    val updateFound = _updateFound.asSharedFlow()

    init {
        if (updatePreferences.enabled().get()) {
            updateChecker.checkForUpdates()
                .onEach {
                    if (it is UpdateChecker.Update.UpdateFound) {
                        _updateFound.emit(it.release)
                    }
                }
                .launchIn(scope)
        }
    }

    override fun onDispose() {
        super.onDispose()
        scope.cancel()
    }
}
