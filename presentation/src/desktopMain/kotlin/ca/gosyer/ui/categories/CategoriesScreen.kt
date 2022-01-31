/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.categories

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import ca.gosyer.presentation.build.BuildKonfig
import ca.gosyer.ui.categories.components.CategoriesScreenContent
import ca.gosyer.ui.util.compose.ThemedWindow
import ca.gosyer.ui.util.lang.launchApplication
import ca.gosyer.uicore.vm.viewModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import cafe.adriel.voyager.core.screen.uniqueScreenKey
import cafe.adriel.voyager.navigator.Navigator
import kotlinx.coroutines.DelicateCoroutinesApi

@OptIn(DelicateCoroutinesApi::class)
fun openCategoriesMenu(notifyFinished: (() -> Unit)? = null) {
    launchApplication {
        ThemedWindow(
            ::exitApplication,
            title = "${BuildKonfig.NAME} - Categories"
        ) {
            Navigator(remember { CategoriesScreen(notifyFinished) })
        }
    }
}

class CategoriesScreen(
    @Transient
    private val notifyFinished: (() -> Unit)? = null
) : Screen {

    override val key: ScreenKey = uniqueScreenKey

    @Composable
    override fun Content() {
        val vm = viewModel<CategoriesScreenViewModel>()
        CategoriesScreenContent(
            categories = vm.categories.collectAsState().value,
            updateRemoteCategories = vm::updateRemoteCategories,
            moveCategoryUp = vm::moveUp,
            moveCategoryDown = vm::moveDown,
            renameCategory = vm::renameCategory,
            deleteCategory = vm::deleteCategory,
            createCategory = vm::createCategory,
            notifyFinished = notifyFinished
        )
    }
}
