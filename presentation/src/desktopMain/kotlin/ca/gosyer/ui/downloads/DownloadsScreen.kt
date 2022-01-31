/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.downloads

import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import ca.gosyer.presentation.build.BuildKonfig
import ca.gosyer.ui.AppComponent
import ca.gosyer.ui.downloads.components.DownloadsScreenContent
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
fun openDownloadsMenu() {
    launchApplication {
        CompositionLocalProvider(*remember { AppComponent.getInstance().uiComponent.getHooks() }) {
            ThemedWindow(::exitApplication, title = BuildKonfig.NAME) {
                Surface {
                    Navigator(remember { DownloadsScreen() })
                }
            }
        }
    }
}

class DownloadsScreen : Screen {
    override val key: ScreenKey = uniqueScreenKey

    @Composable
    override fun Content() {
        val vm = viewModel {
            instantiate<DownloadsScreenViewModel>(false)
        }
        val navigator = LocalNavigator.currentOrThrow
        DownloadsScreenContent(
            downloadQueue = vm.downloadQueue.collectAsState().value,
            downloadStatus = vm.downloaderStatus.collectAsState().value,
            startDownloading = vm::start,
            pauseDownloading = vm::pause,
            clearQueue = vm::clear,
            onMangaClick = { navigator push MangaScreen(it) },
            stopDownload = vm::stopDownload,
            moveDownloadToBottom = vm::moveToBottom
        )
    }
}
