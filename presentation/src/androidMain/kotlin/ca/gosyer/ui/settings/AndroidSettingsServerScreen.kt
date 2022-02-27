/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.settings

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import ca.gosyer.uicore.vm.ContextWrapper
import ca.gosyer.uicore.vm.ViewModel
import me.tatarka.inject.annotations.Inject

@Composable
actual fun getServerHostItems(viewModel: @Composable () -> SettingsServerHostViewModel): LazyListScope.() -> Unit {
    return {}
}

actual class SettingsServerHostViewModel @Inject constructor(contextWrapper: ContextWrapper) : ViewModel(contextWrapper)

