/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.library

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import ca.gosyer.jui.ui.base.screen.BaseScreen
import ca.gosyer.jui.ui.library.components.LibraryScreenContent
import ca.gosyer.jui.ui.library.settings.getLibraryDisplay
import ca.gosyer.jui.ui.library.settings.getLibraryFilters
import ca.gosyer.jui.ui.library.settings.getLibrarySort
import ca.gosyer.jui.ui.manga.MangaScreen
import ca.gosyer.jui.ui.stateViewModel
import ca.gosyer.jui.ui.viewModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow

class LibraryScreen : BaseScreen() {
    @Composable
    override fun Content() {
        val vm = stateViewModel { libraryViewModel(it) }
        val settingsVM = viewModel { librarySettingsViewModel() }
        val updatesVM = viewModel { libraryUpdatesViewModel(false) }
        val navigator = LocalNavigator.currentOrThrow
        LibraryScreenContent(
            libraryState = vm.categories.collectAsState().value,
            selectedCategoryIndex = vm.selectedCategoryIndex.collectAsState().value,
            displayMode = vm.displayMode.collectAsState().value,
            gridColumns = vm.gridColumns.collectAsState().value,
            gridSize = vm.gridSize.collectAsState().value,
            query = vm.query.collectAsState().value,
            updateQuery = vm::updateQuery,
            getLibraryForPage = { vm.getLibraryForCategoryId(it).collectAsState() },
            onPageChanged = vm::setSelectedPage,
            onClickManga = { navigator push MangaScreen(it) },
            onRemoveMangaClicked = vm::removeManga,
            onUpdateLibrary = vm::updateLibrary,
            showingMenu = vm.showingMenu.collectAsState().value,
            setShowingMenu = vm::setShowingMenu,
            libraryFilters = getLibraryFilters(settingsVM),
            librarySort = getLibrarySort(settingsVM),
            libraryDisplay = getLibraryDisplay(settingsVM),
            showUnread = vm.unreadBadges.collectAsState().value,
            showDownloaded = vm.downloadBadges.collectAsState().value,
            showLanguage = vm.languageBadges.collectAsState().value,
            showLocal = vm.localBadges.collectAsState().value,
            updateWebsocketStatus = updatesVM.serviceStatus.collectAsState().value,
            restartLibraryUpdates = updatesVM::restartLibraryUpdates,
        )
    }
}
