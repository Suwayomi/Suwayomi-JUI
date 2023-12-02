/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.categories

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.Window
import ca.gosyer.jui.presentation.build.BuildKonfig
import cafe.adriel.voyager.navigator.Navigator

actual class CategoriesLauncher(
    private val notifyFinished: () -> Unit,
) {
    private var isOpen by mutableStateOf(false)

    actual fun open() {
        isOpen = true
    }

    @Composable
    actual fun CategoriesWindow() {
        if (isOpen) {
            Window(
                onCloseRequest = { isOpen = false },
                title = "${BuildKonfig.NAME} - Categories",
                icon = painterResource("icon.png"),
            ) {
                Navigator(remember { CategoriesScreen(notifyFinished) })
            }
        }
    }
}

@Composable
actual fun rememberCategoriesLauncher(notifyFinished: () -> Unit): CategoriesLauncher =
    remember(notifyFinished) { CategoriesLauncher(notifyFinished) }
