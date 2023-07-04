/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.manga

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import ca.gosyer.jui.ui.manga.components.MangaScreenContent
import ca.gosyer.jui.ui.viewModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.screen.ScreenKey
import cafe.adriel.voyager.core.screen.uniqueScreenKey

class MangaScreen(private val mangaId: Long) : Screen {
    override val key: ScreenKey = uniqueScreenKey

    @Composable
    override fun Content() {
        val vm = viewModel {
            mangaViewModel(MangaScreenViewModel.Params(mangaId))
        }

        MangaScreenContent(
            isLoading = vm.isLoading.collectAsState().value,
            manga = vm.manga.collectAsState().value,
            chapters = vm.chapters.collectAsState().value,
            dateTimeFormatter = vm.dateTimeFormatter.collectAsState().value,
            categoriesExist = vm.categoriesExist.collectAsState().value,
            chooseCategoriesFlowHolder = vm.chooseCategoriesFlowHolder,
            availableCategories = vm.categories.collectAsState().value,
            mangaCategories = vm.mangaCategories.collectAsState().value,
            inActionMode = vm.inActionMode.collectAsState().value,
            selectedItems = vm.selectedItems.collectAsState().value,
            addFavorite = vm::addFavorite,
            setCategories = vm::setCategories,
            toggleFavorite = vm::toggleFavorite,
            refreshManga = vm::refreshManga,
            downloadNext = vm::downloadNext,
            downloadUnread = vm::downloadUnread,
            downloadAll = vm::downloadAll,
            markRead = vm::markRead,
            markUnread = vm::markUnread,
            bookmarkChapter = vm::bookmarkChapter,
            unBookmarkChapter = vm::unBookmarkChapter,
            markPreviousRead = vm::markPreviousRead,
            downloadChapter = vm::downloadChapter,
            deleteDownload = vm::deleteDownload,
            stopDownloadingChapter = vm::stopDownloadingChapter,
            onSelectChapter = vm::selectChapter,
            onUnselectChapter = vm::unselectChapter,
            selectAll = vm::selectAll,
            invertSelection = vm::invertSelection,
            clearSelection = vm::clearSelection,
            downloadChapters = vm::downloadChapters,
            loadChapters = vm::loadChapters,
            loadManga = vm::loadManga,
        )
    }
}
