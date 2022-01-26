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
import ca.gosyer.core.di.AppScope
import toothpick.Toothpick
import toothpick.ktp.binding.module
import toothpick.ktp.extension.getInstance

@Composable
inline fun <reified VM : ViewModel> viewModel(key: Any? = Unit): VM {
    val viewModel = remember(key) {
        AppScope.getInstance<VM>()
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
    crossinline binding: @DisallowComposableCalls () -> Any,
): VM {
    val (viewModel, submodule) = remember(key) {
        val submodule = module {
            binding().let { bind(it.javaClass).toInstance(it) }
        }
        val subscope = AppScope.subscope(submodule).also {
            it.installModules(submodule)
        }
        val viewModel = subscope.getInstance<VM>()
        Pair(viewModel, submodule)
    }
    DisposableEffect(viewModel) {
        onDispose {
            viewModel.destroy()
            Toothpick.closeScope(submodule)
        }
    }
    return viewModel
}
