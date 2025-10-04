/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.main.components

import ca.gosyer.jui.uicore.vm.ContextWrapper
import ca.gosyer.jui.uicore.vm.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import me.tatarka.inject.annotations.Inject

@Inject
actual class DebugOverlayViewModel(
    contextWrapper: ContextWrapper,
) : ViewModel(contextWrapper) {
    actual val maxMemory: String
        get() = ""
    actual val usedMemoryFlow: MutableStateFlow<String> = MutableStateFlow("")
}
