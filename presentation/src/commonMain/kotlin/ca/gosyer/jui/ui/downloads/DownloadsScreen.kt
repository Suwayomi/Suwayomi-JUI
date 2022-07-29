/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.downloads

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import ca.gosyer.jui.ui.downloads.components.DownloadsScreenContent
import ca.gosyer.jui.ui.manga.MangaScreen
import ca.gosyer.jui.ui.viewModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import cafe.adriel.voyager.core.screen.uniqueScreenKey
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow

class DownloadsScreen : Screen {
    override val key: ScreenKey = uniqueScreenKey

    @Composable
    override fun Content() {
        val vm = viewModel { downloadsViewModel(false) }
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
