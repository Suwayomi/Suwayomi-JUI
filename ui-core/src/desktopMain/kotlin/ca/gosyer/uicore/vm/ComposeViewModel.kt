/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.uicore.vm

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisallowComposableCalls
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen

@Composable
inline fun <reified VM : ViewModel> Screen.viewModel(tag: String? = null): VM {
    val viewModelFactory = LocalViewModelFactory.current
    return rememberScreenModel(tag) { viewModelFactory.instantiate() }
}

@Composable
inline fun <reified VM : ViewModel> Screen.viewModel(
    tag: String? = null,
    crossinline factory: @DisallowComposableCalls ViewModelFactory.() -> VM
): VM {
    val viewModelFactory = LocalViewModelFactory.current
    return rememberScreenModel(tag) { viewModelFactory.factory() }
}
