/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.base.vm

import ca.gosyer.ui.base.theme.AppThemeViewModel
import ca.gosyer.ui.categories.CategoriesMenuViewModel
import ca.gosyer.ui.downloads.DownloadsMenuViewModel
import ca.gosyer.ui.extensions.ExtensionsMenuViewModel
import ca.gosyer.ui.library.LibraryScreenViewModel
import ca.gosyer.ui.main.MainViewModel
import ca.gosyer.ui.main.components.DebugOverlayViewModel
import ca.gosyer.ui.main.components.TrayViewModel
import ca.gosyer.ui.manga.MangaMenuViewModel
import ca.gosyer.ui.reader.ReaderMenuViewModel
import ca.gosyer.ui.settings.SettingsAdvancedViewModel
import ca.gosyer.ui.settings.SettingsBackupViewModel
import ca.gosyer.ui.settings.SettingsGeneralViewModel
import ca.gosyer.ui.settings.SettingsLibraryViewModel
import ca.gosyer.ui.settings.SettingsReaderViewModel
import ca.gosyer.ui.settings.SettingsServerViewModel
import ca.gosyer.ui.settings.ThemesViewModel
import ca.gosyer.ui.sources.SourcesMenuViewModel
import ca.gosyer.ui.sources.components.SourceHomeScreenViewModel
import ca.gosyer.ui.sources.components.SourceScreenViewModel
import ca.gosyer.ui.sources.components.filter.SourceFiltersViewModel
import ca.gosyer.ui.sources.settings.SourceSettingsViewModel
import ca.gosyer.ui.updates.UpdatesMenuViewModel
import ca.gosyer.uicore.vm.ViewModel
import ca.gosyer.uicore.vm.ViewModelFactory
import com.github.zsoltk.compose.savedinstancestate.Bundle
import me.tatarka.inject.annotations.Inject
import kotlin.reflect.KClass

@Inject
class ViewModelFactoryImpl(
    private val appThemeFactory: () -> AppThemeViewModel,
    private val categoryFactory: () -> CategoriesMenuViewModel,
    private val downloadsFactory: () -> DownloadsMenuViewModel,
    private val extensionsFactory: () -> ExtensionsMenuViewModel,
    private val libraryFactory: (bundle: Bundle) -> LibraryScreenViewModel,
    private val debugOverlayFactory: () -> DebugOverlayViewModel,
    private val trayFactory: () -> TrayViewModel,
    private val mainFactory: () -> MainViewModel,
    private val mangaFactory: (params: MangaMenuViewModel.Params) -> MangaMenuViewModel,
    private val readerFactory: (params: ReaderMenuViewModel.Params) -> ReaderMenuViewModel,
    private val settingsAdvancedFactory: () -> SettingsAdvancedViewModel,
    private val themesFactory: () -> ThemesViewModel,
    private val settingsBackupFactory: () -> SettingsBackupViewModel,
    private val settingsGeneralFactory: () -> SettingsGeneralViewModel,
    private val settingsLibraryFactory: () -> SettingsLibraryViewModel,
    private val settingsReaderFactory: () -> SettingsReaderViewModel,
    private val settingsServerFactory: () -> SettingsServerViewModel,
    private val sourceFiltersFactory: (params: SourceFiltersViewModel.Params) -> SourceFiltersViewModel,
    private val sourceSettingsFactory: (params: SourceSettingsViewModel.Params) -> SourceSettingsViewModel,
    private val sourceHomeFactory: (bundle: Bundle) -> SourceHomeScreenViewModel,
    private val sourceFactory: (params: SourceScreenViewModel.Params) -> SourceScreenViewModel,
    private val sourcesFactory: (bundle: Bundle) -> SourcesMenuViewModel,
    private val updatesFactory: () -> UpdatesMenuViewModel
): ViewModelFactory() {

    override fun <VM : ViewModel> instantiate(klass: KClass<VM>, arg1: Any?): VM {
        @Suppress("UNCHECKED_CAST", "IMPLICIT_CAST_TO_ANY")
        return when (klass) {
            AppThemeViewModel::class -> appThemeFactory()
            CategoriesMenuViewModel::class -> categoryFactory()
            DownloadsMenuViewModel::class -> downloadsFactory()
            ExtensionsMenuViewModel::class -> extensionsFactory()
            LibraryScreenViewModel::class -> libraryFactory(arg1 as Bundle)
            DebugOverlayViewModel::class -> debugOverlayFactory()
            TrayViewModel::class -> trayFactory()
            MainViewModel::class -> mainFactory()
            MangaMenuViewModel::class -> mangaFactory(arg1 as MangaMenuViewModel.Params)
            ReaderMenuViewModel::class -> readerFactory(arg1 as ReaderMenuViewModel.Params)
            SettingsAdvancedViewModel::class -> settingsAdvancedFactory()
            ThemesViewModel::class -> themesFactory()
            SettingsBackupViewModel::class -> settingsBackupFactory()
            SettingsGeneralViewModel::class -> settingsGeneralFactory()
            SettingsLibraryViewModel::class -> settingsLibraryFactory()
            SettingsReaderViewModel::class -> settingsReaderFactory()
            SettingsServerViewModel::class -> settingsServerFactory()
            SourceFiltersViewModel::class -> sourceFiltersFactory(arg1 as SourceFiltersViewModel.Params)
            SourceSettingsViewModel::class -> sourceSettingsFactory(arg1 as SourceSettingsViewModel.Params)
            SourceHomeScreenViewModel::class -> sourceHomeFactory(arg1 as Bundle)
            SourceScreenViewModel::class -> sourceFactory(arg1 as SourceScreenViewModel.Params)
            SourcesMenuViewModel::class -> sourcesFactory(arg1 as Bundle)
            UpdatesMenuViewModel::class -> updatesFactory()
            else -> throw IllegalArgumentException("Unknown ViewModel $klass")
        } as VM
    }
}
