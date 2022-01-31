/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.library

import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import ca.gosyer.presentation.build.BuildKonfig
import ca.gosyer.ui.AppComponent
import ca.gosyer.ui.library.components.LibraryScreenContent
import ca.gosyer.ui.manga.MangaScreen
import ca.gosyer.ui.util.compose.ThemedWindow
import ca.gosyer.ui.util.lang.launchApplication
import ca.gosyer.uicore.vm.viewModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import cafe.adriel.voyager.core.screen.uniqueScreenKey
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.DelicateCoroutinesApi

@OptIn(DelicateCoroutinesApi::class)
fun openLibraryMenu() {
    launchApplication {
        CompositionLocalProvider(*remember { AppComponent.getInstance().uiComponent.getHooks() }) {
            ThemedWindow(::exitApplication, title = BuildKonfig.NAME) {
                Surface {
                    Navigator(remember { LibraryScreen() })
                }
            }
        }
    }
}

class LibraryScreen : Screen {

    override val key: ScreenKey = uniqueScreenKey

    @Composable
    override fun Content() {
        val vm = viewModel<LibraryScreenViewModel>()
        val navigator = LocalNavigator.currentOrThrow
        LibraryScreenContent(
            categories = vm.categories.collectAsState().value,
            selectedCategoryIndex = vm.selectedCategoryIndex.collectAsState().value,
            displayMode = vm.displayMode.collectAsState().value,
            isLoading = vm.isLoading.collectAsState().value,
            error = vm.error.collectAsState().value,
            query = vm.query.collectAsState().value,
            updateQuery = vm::updateQuery,
            getLibraryForPage = { vm.getLibraryForCategoryId(it).collectAsState() },
            onPageChanged = vm::setSelectedPage,
            onClickManga = { navigator push MangaScreen(it) },
            onRemoveMangaClicked = vm::removeManga
        )
    }
}
