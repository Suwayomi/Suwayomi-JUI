/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.manga

import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import ca.gosyer.presentation.build.BuildKonfig
import ca.gosyer.ui.AppComponent
import ca.gosyer.ui.manga.components.MangaScreenContent
import ca.gosyer.ui.util.compose.ThemedWindow
import ca.gosyer.ui.util.lang.launchApplication
import ca.gosyer.uicore.vm.viewModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import cafe.adriel.voyager.core.screen.uniqueScreenKey
import cafe.adriel.voyager.navigator.Navigator
import kotlinx.coroutines.DelicateCoroutinesApi

@OptIn(DelicateCoroutinesApi::class)
fun openMangaMenu(mangaId: Long) {
    launchApplication {
        CompositionLocalProvider(*remember { AppComponent.getInstance().uiComponent.getHooks() }) {
            ThemedWindow(::exitApplication, title = BuildKonfig.NAME) {
                Surface {
                    Navigator(remember { MangaScreen(mangaId) })
                }
            }
        }
    }
}

class MangaScreen(private val mangaId: Long) : Screen {

    override val key: ScreenKey = uniqueScreenKey

    @Composable
    override fun Content() {
        val vm = viewModel {
            instantiate<MangaScreenViewModel>(MangaScreenViewModel.Params(mangaId))
        }

        MangaScreenContent(
            isLoading = vm.isLoading.collectAsState().value,
            manga = vm.manga.collectAsState().value,
            chapters = vm.chapters.collectAsState().value,
            dateTimeFormatter = vm.dateTimeFormatter.collectAsState().value,
            categoriesExist = vm.categoriesExist.collectAsState().value,
            chooseCategoriesFlow = vm.chooseCategoriesFlow,
            addFavorite = vm::addFavorite,
            setCategories = vm::setCategories,
            toggleFavorite = vm::toggleFavorite,
            refreshManga = vm::refreshManga,
            toggleRead = vm::toggleRead,
            toggleBookmarked = vm::toggleBookmarked,
            markPreviousRead = vm::markPreviousRead,
            downloadChapter = vm::downloadChapter,
            deleteDownload = vm::deleteDownload,
            stopDownloadingChapter = vm::stopDownloadingChapter,
            loadChapters = vm::loadChapters,
            loadManga = vm::loadManga
        )
    }
}
