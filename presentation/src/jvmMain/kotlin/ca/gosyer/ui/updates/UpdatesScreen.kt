/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.updates

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import ca.gosyer.ui.manga.MangaScreen
import ca.gosyer.ui.reader.rememberReaderLauncher
import ca.gosyer.ui.updates.components.UpdatesScreenContent
import ca.gosyer.uicore.vm.viewModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import cafe.adriel.voyager.core.screen.uniqueScreenKey
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow

class UpdatesScreen : Screen {

    override val key: ScreenKey = uniqueScreenKey

    @Composable
    override fun Content() {
        val vm = viewModel<UpdatesScreenViewModel>()
        val navigator = LocalNavigator.currentOrThrow
        val readerLauncher = rememberReaderLauncher()
        UpdatesScreenContent(
            isLoading = vm.isLoading.collectAsState().value,
            updates = vm.updates.collectAsState().value,
            loadNextPage = vm::loadNextPage,
            openChapter = readerLauncher::launch,
            openManga = { navigator push MangaScreen(it) },
            downloadChapter = vm::downloadChapter,
            deleteDownloadedChapter = vm::deleteDownloadedChapter,
            stopDownloadingChapter = vm::stopDownloadingChapter
        )
    }
}
