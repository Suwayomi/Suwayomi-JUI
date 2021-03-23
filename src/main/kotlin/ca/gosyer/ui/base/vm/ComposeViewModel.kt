/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.base.vm

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import org.koin.core.context.GlobalContext

@Composable
inline fun <reified VM : ViewModel> composeViewModel(): VM {
    val viewModel = remember {
        GlobalContext.get().get<VM>()
    }
    DisposableEffect(viewModel) {
        onDispose {
            viewModel.destroy()
        }
    }
    return viewModel
}
