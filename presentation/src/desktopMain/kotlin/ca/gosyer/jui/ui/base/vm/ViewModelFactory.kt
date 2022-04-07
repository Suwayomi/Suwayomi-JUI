/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.base.vm

import ca.gosyer.jui.ui.base.theme.AppThemeViewModel
import ca.gosyer.jui.ui.categories.CategoriesScreenViewModel
import ca.gosyer.jui.ui.downloads.DownloadsScreenViewModel
import ca.gosyer.jui.ui.extensions.ExtensionsScreenViewModel
import ca.gosyer.jui.ui.library.LibraryScreenViewModel
import ca.gosyer.jui.ui.library.settings.LibrarySettingsViewModel
import ca.gosyer.jui.ui.main.MainViewModel
import ca.gosyer.jui.ui.main.about.AboutViewModel
import ca.gosyer.jui.ui.main.components.DebugOverlayViewModel
import ca.gosyer.jui.ui.main.components.TrayViewModel
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
import ca.gosyer.jui.uicore.vm.ViewModelFactory
import me.tatarka.inject.annotations.Inject
import kotlin.reflect.KClass

@Inject
actual class ViewModelFactoryImpl(
    private val aboutFactory: () -> AboutViewModel,
    private val appThemeFactory: () -> AppThemeViewModel,
    private val categoryFactory: () -> CategoriesScreenViewModel,
    private val downloadsFactory: (Boolean) -> DownloadsScreenViewModel,
    private val extensionsFactory: () -> ExtensionsScreenViewModel,
    private val libraryFactory: () -> LibraryScreenViewModel,
    private val librarySettingsFactory: () -> LibrarySettingsViewModel,
    private val debugOverlayFactory: () -> DebugOverlayViewModel,
    private val trayFactory: () -> TrayViewModel,
    private val mainFactory: () -> MainViewModel,
    private val mangaFactory: (params: MangaScreenViewModel.Params) -> MangaScreenViewModel,
    private val readerFactory: (params: ReaderMenuViewModel.Params) -> ReaderMenuViewModel,
    private val settingsAdvancedFactory: () -> SettingsAdvancedViewModel,
    private val themesFactory: () -> ThemesViewModel,
    private val settingsBackupFactory: () -> SettingsBackupViewModel,
    private val settingsGeneralFactory: () -> SettingsGeneralViewModel,
    private val settingsLibraryFactory: () -> SettingsLibraryViewModel,
    private val settingsReaderFactory: () -> SettingsReaderViewModel,
    private val settingsServerFactory: () -> SettingsServerViewModel,
    private val settingsServerHostFactory: () -> SettingsServerHostViewModel,
    private val sourceFiltersFactory: (params: SourceFiltersViewModel.Params) -> SourceFiltersViewModel,
    private val sourceSettingsFactory: (params: SourceSettingsScreenViewModel.Params) -> SourceSettingsScreenViewModel,
    private val sourceHomeFactory: () -> SourceHomeScreenViewModel,
    private val globalSearchFactory: (params: GlobalSearchViewModel.Params) -> GlobalSearchViewModel,
    private val sourceFactory: (params: SourceScreenViewModel.Params) -> SourceScreenViewModel,
    private val updatesFactory: () -> UpdatesScreenViewModel
) : ViewModelFactory() {

    override fun <VM : ViewModel> instantiate(klass: KClass<VM>, arg1: Any?): VM {
        @Suppress("UNCHECKED_CAST", "IMPLICIT_CAST_TO_ANY")
        return when (klass) {
            AboutViewModel::class -> aboutFactory()
            AppThemeViewModel::class -> appThemeFactory()
            CategoriesScreenViewModel::class -> categoryFactory()
            DownloadsScreenViewModel::class -> downloadsFactory(arg1 as Boolean)
            ExtensionsScreenViewModel::class -> extensionsFactory()
            LibraryScreenViewModel::class -> libraryFactory()
            LibrarySettingsViewModel::class -> librarySettingsFactory()
            DebugOverlayViewModel::class -> debugOverlayFactory()
            TrayViewModel::class -> trayFactory()
            MainViewModel::class -> mainFactory()
            MangaScreenViewModel::class -> mangaFactory(arg1 as MangaScreenViewModel.Params)
            ReaderMenuViewModel::class -> readerFactory(arg1 as ReaderMenuViewModel.Params)
            SettingsAdvancedViewModel::class -> settingsAdvancedFactory()
            ThemesViewModel::class -> themesFactory()
            SettingsBackupViewModel::class -> settingsBackupFactory()
            SettingsGeneralViewModel::class -> settingsGeneralFactory()
            SettingsLibraryViewModel::class -> settingsLibraryFactory()
            SettingsReaderViewModel::class -> settingsReaderFactory()
            SettingsServerViewModel::class -> settingsServerFactory()
            SettingsServerHostViewModel::class -> settingsServerHostFactory()
            SourceFiltersViewModel::class -> sourceFiltersFactory(arg1 as SourceFiltersViewModel.Params)
            SourceSettingsScreenViewModel::class -> sourceSettingsFactory(arg1 as SourceSettingsScreenViewModel.Params)
            SourceHomeScreenViewModel::class -> sourceHomeFactory()
            GlobalSearchViewModel::class -> globalSearchFactory(arg1 as GlobalSearchViewModel.Params)
            SourceScreenViewModel::class -> sourceFactory(arg1 as SourceScreenViewModel.Params)
            UpdatesScreenViewModel::class -> updatesFactory()
            else -> throw IllegalArgumentException("Unknown ViewModel $klass")
        } as VM
    }
}
