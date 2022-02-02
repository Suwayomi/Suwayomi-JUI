/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.ui.main

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.NewReleases
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Store
import androidx.compose.material.icons.rounded.Book
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.NewReleases
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Store
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import ca.gosyer.i18n.MR
import ca.gosyer.ui.downloads.DownloadsScreen
import ca.gosyer.ui.extensions.ExtensionsScreen
import ca.gosyer.ui.extensions.openExtensionsMenu
import ca.gosyer.ui.library.LibraryScreen
import ca.gosyer.ui.library.openLibraryMenu
import ca.gosyer.ui.main.components.DownloadsExtraInfo
import ca.gosyer.ui.main.more.MoreScreen
import ca.gosyer.ui.settings.SettingsScreen
import ca.gosyer.ui.sources.SourcesScreen
import ca.gosyer.ui.sources.openSourcesMenu
import ca.gosyer.ui.updates.UpdatesScreen
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.Navigator
import dev.icerock.moko.resources.StringResource
import kotlin.reflect.KClass

interface Menu {
    val textKey: StringResource
    val unselectedIcon: ImageVector
    val selectedIcon: ImageVector
    val screen: KClass<*>
    val createScreen: () -> Screen
    val openInNewWindow: (() -> Unit)?
    val extraInfo: (@Composable () -> Unit)?

    fun isSelected(navigator: Navigator) = navigator.items.first()::class == screen
}

enum class TopLevelMenus(
    override val textKey: StringResource,
    override val unselectedIcon: ImageVector,
    override val selectedIcon: ImageVector,
    override val screen: KClass<*>,
    override val createScreen: () -> Screen,
    override val openInNewWindow: (() -> Unit)? = null,
    override val extraInfo: (@Composable () -> Unit)? = null
) : Menu {
    Library(MR.strings.location_library, Icons.Outlined.Book, Icons.Rounded.Book, LibraryScreen::class, { LibraryScreen() }, ::openLibraryMenu),
    Updates(MR.strings.location_updates, Icons.Outlined.NewReleases, Icons.Rounded.NewReleases, UpdatesScreen::class, { UpdatesScreen() }, ::openLibraryMenu),
    Sources(MR.strings.location_sources, Icons.Outlined.Explore, Icons.Rounded.Explore, SourcesScreen::class, { SourcesScreen() }, ::openSourcesMenu),
    Extensions(MR.strings.location_extensions, Icons.Outlined.Store, Icons.Rounded.Store, ExtensionsScreen::class, { ExtensionsScreen() }, ::openExtensionsMenu),
    More(MR.strings.location_more, Icons.Outlined.MoreHoriz, Icons.Rounded.MoreHoriz, MoreScreen::class, { MoreScreen() });
}

enum class MoreMenus(
    override val textKey: StringResource,
    override val unselectedIcon: ImageVector,
    override val selectedIcon: ImageVector,
    override val screen: KClass<*>,
    override val createScreen: () -> Screen,
    override val openInNewWindow: (() -> Unit)? = null,
    override val extraInfo: (@Composable () -> Unit)? = null
) : Menu {
    Downloads(MR.strings.location_downloads, Icons.Outlined.Download, Icons.Rounded.Download, DownloadsScreen::class, { DownloadsScreen() }, extraInfo = { DownloadsExtraInfo() }),
    Settings(MR.strings.location_settings, Icons.Outlined.Settings, Icons.Rounded.Settings, SettingsScreen::class, { SettingsScreen() });
}