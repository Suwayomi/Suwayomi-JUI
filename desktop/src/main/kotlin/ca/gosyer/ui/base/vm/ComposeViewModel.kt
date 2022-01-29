/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.base.vm

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisallowComposableCalls
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import ca.gosyer.ui.base.LocalViewModelFactory

@Composable
inline fun <reified VM : ViewModel> viewModel(key: Any? = Unit): VM {
    val viewModelFactory = LocalViewModelFactory.current
    val viewModel = remember(key) {
        viewModelFactory.instantiate<VM>()
    }
    DisposableEffect(viewModel) {
        onDispose {
            viewModel.destroy()
        }
    }
    return viewModel
}

@Composable
inline fun <reified VM : ViewModel> viewModel(
    key: Any? = Unit,
    crossinline factory: @DisallowComposableCalls ViewModelFactory.() -> VM
): VM {
    val viewModelFactory = LocalViewModelFactory.current
    val viewModel = remember(key) {
        viewModelFactory.factory()
    }
    DisposableEffect(viewModel) {
        onDispose {
            viewModel.destroy()
        }
    }
    return viewModel
}
