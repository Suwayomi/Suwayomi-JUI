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
import androidx.compose.material.icons.outlined.NewReleases
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Store
import androidx.compose.material.icons.rounded.Book
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.NewReleases
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Store
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import ca.gosyer.i18n.MR
import ca.gosyer.ui.extensions.openExtensionsMenu
import ca.gosyer.ui.library.openLibraryMenu
import ca.gosyer.ui.main.components.DownloadsExtraInfo
import ca.gosyer.ui.sources.openSourcesMenu
import com.github.zsoltk.compose.router.BackStack
import dev.icerock.moko.resources.StringResource

enum class TopLevelMenus(
    val textKey: StringResource,
    val unselectedIcon: ImageVector,
    val selectedIcon: ImageVector,
    val menu: Routes,
    val top: Boolean,
    val openInNewWindow: () -> Unit = {},
    val extraInfo: (@Composable () -> Unit)? = null
) {
    Library(MR.strings.location_library, Icons.Outlined.Book, Icons.Rounded.Book, Routes.Library, true, ::openLibraryMenu),
    Updates(MR.strings.location_updates, Icons.Outlined.NewReleases, Icons.Rounded.NewReleases, Routes.Updates, true, ::openLibraryMenu),
    Sources(MR.strings.location_sources, Icons.Outlined.Explore, Icons.Rounded.Explore, Routes.Sources, true, ::openSourcesMenu),
    Extensions(MR.strings.location_extensions, Icons.Outlined.Store, Icons.Rounded.Store, Routes.Extensions, true, ::openExtensionsMenu),
    Downloads(MR.strings.location_downloads, Icons.Outlined.Download, Icons.Rounded.Download, Routes.Downloads, false, extraInfo = { DownloadsExtraInfo() }),
    Settings(MR.strings.location_settings, Icons.Outlined.Settings, Icons.Rounded.Settings, Routes.Settings, false);

    fun isSelected(backStack: BackStack<Routes>) = backStack.elements.first() == menu
}
