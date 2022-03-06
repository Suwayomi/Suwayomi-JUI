/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.categories

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator

actual class CategoriesLauncher(private val navigator: Navigator?) {

    actual fun open() {
        navigator?.push(CategoriesScreen())
    }

    @Composable
    actual fun CategoriesWindow() {
    }
}

@Composable
actual fun rememberCategoriesLauncher(notifyFinished: () -> Unit): CategoriesLauncher {
    val navigator = LocalNavigator.current
    return remember(navigator) { CategoriesLauncher(navigator) }
}
