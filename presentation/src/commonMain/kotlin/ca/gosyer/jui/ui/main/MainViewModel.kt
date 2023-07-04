/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.main

import ca.gosyer.jui.domain.ui.service.UiPreferences
import ca.gosyer.jui.i18n.MR
import ca.gosyer.jui.uicore.vm.ContextWrapper
import ca.gosyer.jui.uicore.vm.ViewModel
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import me.tatarka.inject.annotations.Inject

class MainViewModel
    @Inject
    constructor(
        uiPreferences: UiPreferences,
        contextWrapper: ContextWrapper,
    ) : ViewModel(contextWrapper) {
        override val scope = MainScope()

        val startScreen = uiPreferences.startScreen().get()
        val confirmExit = uiPreferences.confirmExit().stateIn(scope)

        override fun onDispose() {
            super.onDispose()
            scope.cancel()
        }

        fun confirmExitToast() {
            toast(MR.strings.confirm_exit_toast.toPlatformString())
        }
    }
