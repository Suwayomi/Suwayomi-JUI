/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.categories

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import ca.gosyer.presentation.build.BuildKonfig
import ca.gosyer.ui.AppComponent
import ca.gosyer.ui.util.compose.ThemedWindow
import ca.gosyer.ui.util.lang.launchApplication
import cafe.adriel.voyager.navigator.Navigator
import kotlinx.coroutines.DelicateCoroutinesApi

@OptIn(DelicateCoroutinesApi::class)
actual fun openCategoriesMenu(notifyFinished: () -> Unit, navigator: Navigator) {
    launchApplication {
        CompositionLocalProvider(*remember { AppComponent.getInstance().uiComponent.getHooks() }) {
            ThemedWindow(::exitApplication, title = "${BuildKonfig.NAME} - Categories") {
                Navigator(remember { CategoriesScreen(notifyFinished) })
            }
        }
    }
}