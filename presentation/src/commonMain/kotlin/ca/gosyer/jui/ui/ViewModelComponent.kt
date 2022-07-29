/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisallowComposableCalls
import ca.gosyer.jui.ui.base.LocalViewModels
import ca.gosyer.jui.ui.base.theme.AppThemeViewModel
import ca.gosyer.jui.ui.categories.CategoriesScreenViewModel
import ca.gosyer.jui.ui.downloads.DownloadsScreenViewModel
import ca.gosyer.jui.ui.extensions.ExtensionsScreenViewModel
import ca.gosyer.jui.ui.library.LibraryScreenViewModel
import ca.gosyer.jui.ui.library.settings.LibrarySettingsViewModel
import ca.gosyer.jui.ui.main.MainViewModel
import ca.gosyer.jui.ui.main.about.AboutViewModel
import ca.gosyer.jui.ui.main.components.DebugOverlayViewModel
import ca.gosyer.jui.ui.manga.MangaScreenViewModel
import ca.gosyer.jui.ui.reader.ReaderMenuViewModel
import ca.gosyer.jui.ui.settings.SettingsAdvancedViewModel
import ca.gosyer.jui.ui.settings.SettingsBackupViewModel
import ca.gosyer.jui.ui.settings.SettingsGeneralViewModel
import ca.gosyer.jui.ui.settings.SettingsLibraryViewModel
import ca.gosyer.jui.ui.settings.SettingsReaderViewModel
import ca.gosyer.jui.ui.settings.SettingsServerHostViewModel
import ca.gosyer.jui.ui.settings.SettingsServerViewModel
import ca.gosyer.jui.ui.settings.ThemesViewModel
import ca.gosyer.jui.ui.sources.browse.SourceScreenViewModel
import ca.gosyer.jui.ui.sources.browse.filter.SourceFiltersViewModel
import ca.gosyer.jui.ui.sources.globalsearch.GlobalSearchViewModel
import ca.gosyer.jui.ui.sources.home.SourceHomeScreenViewModel
import ca.gosyer.jui.ui.sources.settings.SourceSettingsScreenViewModel
import ca.gosyer.jui.ui.updates.UpdatesScreenViewModel
import ca.gosyer.jui.uicore.vm.ViewModel
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen

interface SharedViewModelComponent {
    val aboutViewModel: () -> AboutViewModel
    val appThemeViewModel: () -> AppThemeViewModel
    val categoryViewModel: () -> CategoriesScreenViewModel
    val downloadsViewModel: (Boolean) -> DownloadsScreenViewModel
    val extensionsViewModel: () -> ExtensionsScreenViewModel
    val libraryViewModel: () -> LibraryScreenViewModel
    val librarySettingsViewModel: () -> LibrarySettingsViewModel
    val debugOverlayViewModel: () -> DebugOverlayViewModel
    val mainViewModel: () -> MainViewModel
    val mangaViewModel: (params: MangaScreenViewModel.Params) -> MangaScreenViewModel
    val readerViewModel: (params: ReaderMenuViewModel.Params) -> ReaderMenuViewModel
    val settingsAdvancedViewModel: () -> SettingsAdvancedViewModel
    val themesViewModel: () -> ThemesViewModel
    val settingsBackupViewModel: () -> SettingsBackupViewModel
    val settingsGeneralViewModel: () -> SettingsGeneralViewModel
    val settingsLibraryViewModel: () -> SettingsLibraryViewModel
    val settingsReaderViewModel: () -> SettingsReaderViewModel
    val settingsServerViewModel: () -> SettingsServerViewModel
    val settingsServerHostViewModel: () -> SettingsServerHostViewModel
    val sourceFiltersViewModel: (params: SourceFiltersViewModel.Params) -> SourceFiltersViewModel
    val sourceSettingsViewModel: (params: SourceSettingsScreenViewModel.Params) -> SourceSettingsScreenViewModel
    val sourceHomeViewModel: () -> SourceHomeScreenViewModel
    val globalSearchViewModel: (params: GlobalSearchViewModel.Params) -> GlobalSearchViewModel
    val sourceViewModel: (params: SourceScreenViewModel.Params) -> SourceScreenViewModel
    val updatesViewModel: () -> UpdatesScreenViewModel
}

expect interface ViewModelComponent : SharedViewModelComponent

@Composable
inline fun <reified VM : ViewModel> Screen.viewModel(
    tag: String? = null,
    crossinline factory: @DisallowComposableCalls ViewModelComponent.() -> VM
): VM {
    val viewModelFactory = LocalViewModels.current
    return rememberScreenModel(tag) { viewModelFactory.factory() }
}
